package ai.anam.lab.webrtc

import ai.anam.lab.ConnectionClosedReason
import ai.anam.lab.SessionEvent
import ai.anam.lab.api.DataChannelMessage
import ai.anam.lab.api.SessionConfig
import ai.anam.lab.api.SignalMessage
import ai.anam.lab.api.SignalMessagePayload
import ai.anam.lab.api.SignalMessageType
import ai.anam.lab.api.UserDataMessage
import ai.anam.lab.api.asRaw
import ai.anam.lab.api.defaultJsonConfiguration
import ai.anam.lab.utils.Logger
import ai.anam.lab.utils.cancellableRunCatching
import ai.anam.lab.webrtc.MediaStreamManagerImpl.MediaAccessException
import com.shepeliev.webrtckmp.DataChannel
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.IceConnectionState
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.PeerConnectionState
import com.shepeliev.webrtckmp.audioTracks
import com.shepeliev.webrtckmp.onConnectionStateChange
import com.shepeliev.webrtckmp.onIceCandidate
import com.shepeliev.webrtckmp.onIceConnectionStateChange
import com.shepeliev.webrtckmp.onSignalingStateChange
import com.shepeliev.webrtckmp.onTrack
import io.ktor.utils.io.core.toByteArray
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

internal interface StreamingClient {
    /**
     * Blocking call to connect via WebRTC. When no longer required, cancel the associated [CoroutineScope].
     */
    suspend fun connect()

    /**
     * Sends a [UserDataMessage] via the WebRTC [DataChannel].
     *
     * @return `true` if the message was sent successfully, `false` otherwise.
     */
    fun sendDataMessage(message: UserDataMessage): Boolean

    /**
     * A flow of [SessionEvent]s that the Streaming Client is responsible for reporting.
     */
    val events: Flow<SessionEvent>

    /**
     * A flow of [DataChannelMessage]s that have been received.
     */
    val dataChannelMessages: Flow<DataChannelMessage>
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalAtomicApi::class)
internal class StreamingClientImpl(
    private val config: SessionConfig,
    private val isLocalAudioEnabled: Boolean,
    private val mediaStreamManager: MediaStreamManager,
    private val signallingClient: SignallingClient,
    private val logger: Logger,
    private val json: Json = defaultJsonConfiguration,
) : StreamingClient {

    // We store the current PeerConnection as a Flow, so that we can detect changes and map those to appropriate states.
    private var _peerConnection = MutableStateFlow<PeerConnection?>(null)
    val peerConnection = _peerConnection.asStateFlow().filterNotNull()

    // After a PeerConnection has been established, we then create a dedicated DataChannel. This will be used to both
    // send and receive messages via WebRTC. These messages will includes events, just as text updates on what is being
    // said (both by the user and persona).
    private var dataChannel = MutableStateFlow<DataChannel?>(null)
    override val dataChannelMessages = dataChannel
        .filterNotNull()
        .flatMapLatest { value -> value.onMessage }
        .mapNotNull { message ->
            cancellableRunCatching {
                json.decodeFromString<DataChannelMessage>(message.decodeToString())
            }.onFailure { ex ->
                logger.e(TAG, ex) { "Error decoding data channel message" }
            }.getOrNull()
        }

    // Flag to record whether or not we've received an Answer via our Signalling Client.
    private val connectionReceivedAnswer = AtomicBoolean(false)

    // Store the set of IceCandidates until we've received a suitable Answer message, at which we can add them to our
    // PeerConnection.
    private val remoteIceCandidatesMutex = Mutex()
    private val remoteIceCandidates = mutableListOf<IceCandidate>()

    private val remoteTrack = peerConnection.flatMapLatest { peerConnection -> peerConnection.onTrack }

    // The event bus.
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 5)
    override val events: Flow<SessionEvent> = _events.asSharedFlow()

    /**
     * @throws MediaAccessException Thrown if we've been configured to use local audio, but the necessary permission
     * @throws MediaAccessException Thrown if we've been configured to use local audio, but the necessary permission
     * has not been granted.
     */
    override suspend fun connect() = coroutineScope {
        if (_peerConnection.value != null) {
            logger.w(TAG) { "Peer connection already exists, skipping initialization" }
            return@coroutineScope
        }

        // If local audio is required, let's see if we can access it before attempting to initialize the connection.
        val localAudioStream = if (isLocalAudioEnabled) {
            runCatching {
                mediaStreamManager.initializeLocalAudio()
            }.onFailure {
                logger.e(TAG, it) { "Failed to initialize local audio stream" }
                disconnect(ConnectionClosedReason.MicrophonePermissionDenied)
                return@coroutineScope
            }.getOrNull()
        } else {
            null
        }

        suspendCancellableCoroutine { continuation ->
            logger.i(TAG) { "Initializing WebRTC peer connection..." }

            // Create the peer connection with configuration provided.
            val config = config.clientConfig.toRtcConfiguration()
            val localPeerConnection = PeerConnection(config).also {
                logger.i(TAG) { "Peer connection created with ${config.iceServers.size} ICE servers" }
                _peerConnection.value = it
            }

            // Create the DataChannel to send/receive messages.
            val localDataChannel = localPeerConnection.createDataChannel(
                label = "session",
                ordered = true,
            )

            if (localDataChannel == null) {
                logger.e(TAG) { "Failed to create data channel" }
                disconnect(ConnectionClosedReason.WebRtcFailure("Failed to create data channel"))
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }

            launch {
                logger.i(TAG) { "Connecting to data channel" }
                localDataChannel.onOpen.collect {
                    logger.i(TAG) { "Data channel connected" }
                    dataChannel.value = localDataChannel
                }
            }

            localAudioStream?.let {
                // Add local audio track to peer connection
                localAudioStream.audioTracks.forEach { track ->
                    localPeerConnection.addTrack(track, localAudioStream).let {
                        logger.i(TAG) { "Added local audio track to peer connection" }
                    }
                }

                // Notify that we've configured our local audio stream.
                _events.tryEmit(SessionEvent.InputAudioStreamStarted)
            }

            // As we detect remote streams, store them via our MediaStreamManager.
            launch {
                remoteTrack
                    .mapNotNull { event -> event.streams.firstOrNull() }
                    .distinctUntilChanged { old, new -> old.id == new.id }
                    .collect { stream ->
                        logger.i(TAG) { "Remote stream received" }
                        mediaStreamManager.setRemoteStream(stream)
                    }
            }

            launch {
                localPeerConnection.onIceCandidate.collect { candidate ->
                    logger.i(TAG) { "Ice Candidate: $candidate" }
                    signallingClient.sendIceCandidate(candidate)
                }
            }

            // Start listening to any messages received by the SignallingClient.
            launch {
                signallingClient.received.collect { message ->
                    onSignalMessage(message)
                }
            }

            // Finally, let's create an Offer that we can communicate via our SignallingClient.
            launch {
                val offer = localPeerConnection.createOffer(
                    options = OfferAnswerOptions(
                        offerToReceiveVideo = true,
                        offerToReceiveAudio = true,
                    ),
                ).also {
                    localPeerConnection.setLocalDescription(it)
                }

                logger.i(TAG) { "Sending Offer: $offer" }
                signallingClient.sendOffer(offer)
            }

            launch {
                peerConnection.flatMapLatest { peerConnection ->
                    peerConnection.onConnectionStateChange
                }.collect { state ->
                    onConnectionStateChange(state)
                }
            }

            launch {
                peerConnection.flatMapLatest { peerConnection ->
                    peerConnection.onIceConnectionStateChange
                }.collect { state ->
                    onIceConnectionStateChange(state)
                }
            }

            if (DEBUG) {
                launch {
                    peerConnection.flatMapLatest { peerConnection ->
                        peerConnection.onSignalingStateChange
                    }.collect { state ->
                        logger.d(TAG) { "SignallingSate: $state" }
                    }
                }
            }

            logger.i(TAG) { "WebRTC initialization complete" }

            // We will now block until the coroutine is cancelled.
            continuation.invokeOnCancellation {
                disconnect(ConnectionClosedReason.Normal)
            }
        }
    }

    /**
     * Attempts to send a [UserDataMessage] via the WebRTC [DataChannel].
     */
    override fun sendDataMessage(message: UserDataMessage): Boolean {
        val raw = runCatching { json.encodeToString(message) }.getOrNull() ?: run {
            logger.e(TAG) { "Failed to encode UserDataMessage" }
            return false
        }

        return sendDataMessage(raw)
    }

    /**
     * Attempts to send a raw [String] via the WebRTC [DataChannel].
     */
    internal fun sendDataMessage(message: String): Boolean {
        val result = dataChannel.value?.send(message.toByteArray()) == true
        if (!result) {
            logger.e(TAG) { "Failed to send data message" }
        }
        return result
    }

    /**
     * Disconnects the peer connection and cleans up all resources.
     */
    private fun disconnect(reason: ConnectionClosedReason) {
        val connection = _peerConnection.value ?: return

        logger.i(TAG) { "Disconnecting WebRTC peer connection ($reason)..." }
        connection.close()
        _peerConnection.value = null

        _events.tryEmit(SessionEvent.ConnectionClosed(reason))
        logger.i(TAG) { "WebRTC disconnected and cleaned up" }
    }

    /**
     * Handles any received messages from the [SignallingClient].
     */
    private suspend fun onSignalMessage(message: SignalMessage) {
        logger.i(TAG) { "Message Received: $message" }

        when (message.payload) {
            is SignalMessagePayload.RTCSessionDescription -> {
                _peerConnection.value?.setRemoteDescription(message.payload.toSessionDescription())

                // Flush any received Ice Candidates.
                remoteIceCandidatesMutex.withLock {
                    connectionReceivedAnswer.store(true)
                    remoteIceCandidates.forEach { candidate ->
                        _peerConnection.value?.addIceCandidate(candidate)
                    }

                    remoteIceCandidates.clear()
                }
            }

            is SignalMessagePayload.RTCIceCandidate -> {
                val candidate = message.payload.toIceCandidate()

                // If we've already received an Answer, then we can immediately add the IceCandidate. Otherwise, we need
                // to store to later process.
                remoteIceCandidatesMutex.withLock {
                    if (connectionReceivedAnswer.load()) {
                        _peerConnection.value?.addIceCandidate(candidate)
                    } else {
                        remoteIceCandidates.add(candidate)
                    }
                }
            }

            else -> Unit
        }

        when (message.actionType) {
            SignalMessageType.SessionReady -> {
                _events.emit(SessionEvent.SessionReady(message.sessionId))
            }

            SignalMessageType.EndSession -> {
                // The server has requested that we disconnect.
                val reason = message.payload.asRaw().ifEmpty { message.payload.toString() }
                disconnect(ConnectionClosedReason.ServerConnectionClosed(reason))
            }

            else -> Unit
        }
    }

    private fun onConnectionStateChange(state: PeerConnectionState) {
        logger.d(TAG) { "PeerConnectionState: $state" }

        if (state == PeerConnectionState.Closed) {
            disconnect(
                ConnectionClosedReason.WebRtcFailure(
                    "The connection to our servers was lost. Please try again.",
                ),
            )
        }
    }

    private suspend fun onIceConnectionStateChange(state: IceConnectionState) {
        logger.d(TAG) { "IceConnectionState: $state" }

        // Check the status of our IceConnection to determine when our session/connection has been established.
        if (state == IceConnectionState.Connected || state == IceConnectionState.Completed) {
            _events.emit(SessionEvent.ConnectionEstablished)
        }
    }

    private companion object {
        const val TAG = "StreamingClient"

        // Local flag (for now) to allow us to log more debug/verbose information.
        const val DEBUG = true
    }
}
