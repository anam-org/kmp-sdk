package ai.anam.lab

import ai.anam.lab.api.UserDataMessage
import ai.anam.lab.utils.Logger
import ai.anam.lab.webrtc.MediaStreamManager
import ai.anam.lab.webrtc.MessagingClient
import ai.anam.lab.webrtc.SignallingClient
import ai.anam.lab.webrtc.StreamingClient
import kotlin.time.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch

/**
 * This class represents an active session with the Anam SDK. The call to [start] will begin the session, and its
 * lifetime is linked to the CoroutineScope of the call. When the scope is cancelled, the Session will be cleaned up
 * along with any obtained resources.
 */
public class Session internal constructor(
    public val id: String,
    private val signallingClient: SignallingClient,
    private val streamingClient: StreamingClient,
    private val mediaStreamManager: MediaStreamManager,
    private val messagingClient: MessagingClient,
    private val sessionManager: PlatformSessionManager,
    private val logger: Logger,
    private val isLoggingEnabled: Boolean = true,
    private val clock: Clock = Clock.System,
) {
    /**
     * Flag to specify whether or not the session is actively streaming.
     */
    public var isActive: Boolean = false
        private set

    /**
     * Flag to mute or unmute the local audio stream (microphone). This can be set before the session has started.
     */
    public var isLocalAudioMuted: Boolean
        get() = mediaStreamManager.isLocalAudioMuted
        set(value) {
            mediaStreamManager.isLocalAudioMuted = value
        }

    /**
     * Access to the VideoTrack that the Persona will be rendered too. This is internal, to allow it to be accessed by
     * the [AnamVideo] component. The [Session] is provided to the [AnamVideo] Composable, which will be able to access
     * this track to render to the local Surface.
     */
    internal val remoteVideoTrack = mediaStreamManager.remoteVideoTrack

    // A local flow of events that the session can emit itself (e.g. first video frame rendered).
    private val localEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)

    /**
     * The [SessionEvent]s associated with this [Session].
     */
    public val events: Flow<SessionEvent> = merge(
        signallingClient.events,
        streamingClient.events,
        localEvents.asSharedFlow(),
    ).transformWhile { event ->
        // When we receive a ConnectionClosed event, it indicates that it's terminal. We should not emit any further
        // events as we tear down our connection(s). This avoids any duplicated events with different "reasons" (or
        // codes)
        emit(event)
        event !is SessionEvent.ConnectionClosed
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    /**
     * The [Message]s associated with this [Session].
     */
    public val messages: Flow<List<Message>> = _messages.asStateFlow()

    // Internally, we track all the jobs we have scheduled. This will allow us to cancel them as soon as we detect an
    // error with any one of our required infrastructure.
    private val jobs = mutableListOf<Job>()

    public suspend fun start(): Unit = coroutineScope {
        logger.i(TAG) { "Starting session..." }
        localEvents.emit(SessionEvent.Connecting)

        isActive = true
        try {
            // Since we're starting the Session, let's connect to our various Clients as well as configure any platform
            // specific management.
            jobs += listOf(
                signallingClient::connect,
                streamingClient::connect,

                // The platform specific session manager is responsible for dealing with any local resources that need
                // to be managed during the lifetime of the session. For example, any network lock or audio focus
                // requests.
                sessionManager::start,
            ).map { action ->
                launch { action() }
            }

            // Collect any incoming messages from the MessagingClient so that we can expose them to the consumer. If we
            // just provided direct access to the flow from the MessagingClient, then these messages would only be
            // available if collected. This way, we tied the set of messages to the scope of this Session.
            jobs += launch {
                messagingClient.messages.collect { messages ->
                    _messages.value = messages
                }
            }

            if (isLoggingEnabled) {
                jobs += launch {
                    messages.collect { message ->
                        logger.i(TAG) { "Message History: $message" }
                    }
                }

                jobs += launch {
                    events.collect { event ->
                        logger.i(TAG) { "Session Event: $event" }
                    }
                }
            }

            awaitConnectionClosed()
        } finally {
            isActive = false

            // If we've detected that our connection has been closed, we need to manually cancel all of our jobs. This
            // will prevent us from keeping one connection open while the other(s) have failed. In the event of our
            // main coroutine context being cancelled, these will effectively be a no-op.
            jobs.forEach { job -> job.cancel() }
            jobs.clear()

            // Release any additional resources.
            mediaStreamManager.release()
        }
    }

    /**
     * Sends a user text message in the active streaming session.
     *
     * @throws IllegalStateException if the session is not active.
     */
    public fun sendUserMessage(content: String) {
        if (!isActive) {
            error("Session is not active")
        }

        // Attempt to send the Message through our WebRTC DataChannel.
        streamingClient.sendDataMessage(
            UserDataMessage.UserTextMessage(
                content = content,
                sessionId = id,
                timestamp = getCurrentTimestamp(),
            ),
        )
    }

    /**
     * Interrupts the Persona in the active streaming session.
     *
     * @throws IllegalStateException if the session is not active.
     */
    public fun interruptPersona() {
        if (!isActive) {
            error("Session is not active")
        }

        // Attempt to send the Message through our WebRTC DataChannel.
        streamingClient.sendDataMessage(
            UserDataMessage.PersonaInterruptMessage(
                sessionId = id,
                timestamp = getCurrentTimestamp(),
            ),
        )
    }

    internal fun onFirstFrameRendered() {
        // The attached VideoSession has notified us that it's been able to render its first frame. Let's emit this
        // so that any consumer can also detect it.
        localEvents.tryEmit(SessionEvent.VideoPlayStarted)
    }

    /**
     * Helper function to wait for the first [SessionEvent.ConnectionClosed] event.
     */
    private suspend fun awaitConnectionClosed() {
        events.first { event ->
            event is SessionEvent.ConnectionClosed
        }
    }

    /**
     * Helper function to generate a timestamp suitable for [UserDataMessage]'s.
     */
    private fun getCurrentTimestamp(): String = clock.now().toString().replace("Z", "")

    private companion object Companion {
        const val TAG = "Session"
    }
}
