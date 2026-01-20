package ai.anam.lab.webrtc

import ai.anam.lab.ApiGateway
import ai.anam.lab.ConnectionClosedReason
import ai.anam.lab.SessionEvent
import ai.anam.lab.api.SessionConfig
import ai.anam.lab.api.SignalMessage
import ai.anam.lab.api.SignalMessagePayload
import ai.anam.lab.api.SignalMessageType
import ai.anam.lab.utils.Logger
import ai.anam.lab.utils.cancellableRunCatching
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.SessionDescription
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.path
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal interface SignallingClient {
    /**
     * Blocking call to connect to our signalling infrastructure. When no longer required, cancel the associated
     * [CoroutineScope].
     */
    suspend fun connect()

    /**
     * The current connection state of the client.
     */
    val connected: Flow<Boolean>

    /**
     * A flow of [SessionEvent]s that the Signalling Client is responsible for reporting.
     */
    val events: Flow<SessionEvent>

    /**
     * Flow of received [SignalMessage]s.
     */
    val received: Flow<SignalMessage>

    /**
     * Send an offer.
     */
    suspend fun sendOffer(localDescription: SessionDescription)

    /**
     * Send an ICE candidate.
     */
    suspend fun sendIceCandidate(candidate: IceCandidate)

    /**
     * Send a talk message.
     */
    suspend fun sendTalkMessage(content: String, startOfSpeech: Boolean, endOfSpeech: Boolean, correlationId: String)
}

internal class SignallingClientImpl(
    private val config: SessionConfig,
    private val apiGateway: ApiGateway?,
    private val httpClient: HttpClient,
    private val logger: Logger,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SignallingClient {
    // The expected interval between heartbeats.
    private val heartbeatInterval by lazy {
        (config.clientConfig.expectedHeartbeatIntervalSecs ?: DEFAULT_HEARTBEAT_INTERVAL_SECONDS).seconds
    }

    // The maximum amount of attempts we can try and reconnect.
    private val maxReconnectionAttempts by lazy {
        config.clientConfig.maxWsReconnectAttempts ?: DEFAULT_WS_RECONNECTION_ATTEMPTS
    }

    private val url by lazy {
        // Build the default Target URL. This will either be used directly, or via a Gateway.
        val targetUrl = URLBuilder().apply {
            protocol = if (URLProtocol.createOrDefault(config.engineProtocol) == URLProtocol.HTTP) {
                URLProtocol.WS
            } else {
                URLProtocol.WSS
            }

            // The engine host could potentially contain the (or part of the) path, so we need to strip it.
            host = config.engineHost.split("/").first()

            path(config.signallingEndpoint ?: DEFAULT_SIGNALLING_PATH)
            parameters.append(PARAM_SESSION_ID, config.sessionId)
        }.build()

        if (apiGateway != null) {
            // Build the base of the Gateway URL.
            val gatewayUrl = URLBuilder(apiGateway.baseUrl).apply {
                path(apiGateway.wsPath ?: DEFAULT_SIGNALLING_PATH)
            }.build()

            // Update the Gateway URL to include details of our Target.
            URLBuilder(gatewayUrl).apply {
                parameters.append(PARAM_TARGET_URL, targetUrl.toString())
            }.build()
        } else {
            targetUrl
        }
    }

    // We use a buffered channel to have our consumers emit into immediately. Then separately, we will process these
    // sequentially when emitting them via the Web Socket.
    private val outgoingChannel = Channel<SignalMessage>(Channel.BUFFERED)

    // The current connected state of the client.
    private val _connected = MutableStateFlow(false)
    override val connected: Flow<Boolean> = _connected.asStateFlow()

    // The event bus.
    private val _events = MutableSharedFlow<SessionEvent>()
    override val events: Flow<SessionEvent> = _events.asSharedFlow()

    // Messages which are received are emitted to our Shared Flow. These can then be collected by consumers and filtered
    // based upon type.
    private val _received = MutableSharedFlow<SignalMessage>()
    override val received = _received.asSharedFlow()

    override suspend fun connect() = coroutineScope {
        withContext(ioDispatcher) {
            var count = 0
            while (count <= maxReconnectionAttempts) {
                try {
                    logger.i(TAG) { "Attempting to connected via WebSocket (attempt: $count, url: $url)" }
                    count++

                    httpClient.webSocket(urlString = url.toString()) {
                        logger.i(TAG) { "WebSocket Connected" }
                        _connected.value = true

                        // Reset our reconnection attempts now that we've successfully connected.
                        count = 0

                        // Launch a Job that will process our Outgoing Channel. As new messages are provided to us,
                        // these will be appended to our channel and processed here. If we're not connected at the time
                        // of being provided the message, they are buffered in the Channel until we're ready to process
                        // them.
                        launch {
                            for (message in outgoingChannel) {
                                cancellableRunCatching {
                                    sendSerialized(message)
                                }.onFailure {
                                    logger.e(TAG, it) { "Failed to send message: $message" }
                                }
                            }

                            logger.e(TAG) { "Run out of outgoing messages..." }
                        }

                        // Launch a Job that will process our incoming message stream. Once received and deserialized,
                        // we'll emit them to our Shared Flow to be handled by any collector.
                        launch {
                            while (isActive) {
                                cancellableRunCatching {
                                    val message = receiveDeserialized<SignalMessage>()
                                    logger.i(TAG) { "Message Received: $message" }

                                    _received.emit(message)
                                }.onFailure {
                                    logger.e(TAG, it) { "Failed to process received message" }
                                }
                            }
                        }

                        launch {
                            // While connected, we will periodically sent a Heartbeat message to notify that this client
                            // is still alive and connected.
                            while (isActive) {
                                delay(heartbeatInterval)
                                sendHeartbeat()
                            }
                        }

                        // Now that we have everything configured, we can wait until our associated coroutine scope is
                        // cancelled.
                        awaitCancellation()
                    }
                } catch (cancel: CancellationException) {
                    // A CancellationException means that our CoroutineScope has been cancelled. This means we should
                    // disconnect and clean up any resources.
                    logger.i(TAG) { "WebSocket signalling cancelled" }
                    throw cancel
                } catch (ex: Exception) {
                    logger.e(TAG, ex) { "WebSocket Failed: ${ex.message}" }
                    delay((count * 100).milliseconds)
                } finally {
                    // Since we've either failed to connect, or failed to reconnect, make sure our connected status is
                    // up to date.
                    _connected.value = false
                }
            }

            // We've now failed to connect after our maximum number of attempts.
            _events.emit(
                SessionEvent.ConnectionClosed(
                    ConnectionClosedReason.SignallingClientConnectionFailure(
                        message = "Max Reconnection Attempts Reached ($maxReconnectionAttempts)",
                    ),
                ),
            )
        }
    }

    override suspend fun sendOffer(localDescription: SessionDescription) {
        logger.i(TAG) { "Sending offer: $localDescription" }
        outgoingChannel.send(
            SignalMessage(
                actionType = SignalMessageType.Offer,
                sessionId = config.sessionId,
                payload = SignalMessagePayload.Offer(
                    sessionId = config.sessionId,
                    connectionDescription = localDescription.toRTCSessionDescription(),
                ),
            ),
        )
    }

    override suspend fun sendIceCandidate(candidate: IceCandidate) {
        logger.i(TAG) { "Sending ICE candidate: $candidate" }
        outgoingChannel.send(
            SignalMessage(
                actionType = SignalMessageType.IceCandidate,
                sessionId = config.sessionId,
                payload = candidate.toRTCIceCandidate(),
            ),
        )
    }

    override suspend fun sendTalkMessage(
        content: String,
        startOfSpeech: Boolean,
        endOfSpeech: Boolean,
        correlationId: String,
    ) {
        logger.i(TAG) { "Sending Talk message: $content" }
        outgoingChannel.send(
            SignalMessage(
                actionType = SignalMessageType.TalkStreamInput,
                sessionId = config.sessionId,
                payload = SignalMessagePayload.TalkMessage(
                    content = content,
                    startOfSpeech = startOfSpeech,
                    endOfSpeech = endOfSpeech,
                    correlationId = correlationId,
                ),
            ),
        )
    }

    private fun sendHeartbeat() {
        logger.d(TAG) { "Sending Heartbeat" }
        outgoingChannel.trySend(
            SignalMessage(
                actionType = SignalMessageType.Heartbeat,
                sessionId = config.sessionId,
                payload = SignalMessagePayload.Empty,
            ),
        )
    }

    private companion object {
        const val TAG = "SignallingClient"

        const val DEFAULT_SIGNALLING_PATH = "/ws"
        const val DEFAULT_HEARTBEAT_INTERVAL_SECONDS = 5
        const val DEFAULT_WS_RECONNECTION_ATTEMPTS = 5

        const val PARAM_SESSION_ID = "session_id"
        const val PARAM_TARGET_URL = "target_url"
    }
}
