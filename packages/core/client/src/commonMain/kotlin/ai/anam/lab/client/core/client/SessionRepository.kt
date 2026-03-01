package ai.anam.lab.client.core.client

import ai.anam.lab.AnamClient
import ai.anam.lab.PersonaConfig
import ai.anam.lab.Session
import ai.anam.lab.SessionEvent
import ai.anam.lab.SessionOptions
import ai.anam.lab.SessionResult
import ai.anam.lab.client.core.auth.AuthRepository
import ai.anam.lab.client.core.data.models.Persona
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.createUnsafeSession
import ai.anam.lab.utils.UnsafeAnamApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Success : SessionState
    data object Failed : SessionState
}

@OptIn(UnsafeAnamApi::class)
@Inject
@SingleIn(AppScope::class)
class SessionRepository(
    private val authRepository: AuthRepository,
    private val client: AnamClient,
    private val logger: Logger,
    private val coroutineScope: CoroutineScope,
) {

    val activeSession: StateFlow<Session?>
        field = MutableStateFlow<Session?>(null)
    private val actionSessionJobs = mutableListOf<Job>()

    val isAudioMute: StateFlow<Boolean>
        field = MutableStateFlow(false)

    suspend fun startSession(person: Persona): SessionState {
        // Before starting any session, let's make sure a previous one has been cleaned up.
        stopSession()

        // Check that we have a valid API token.
        val apiToken = authRepository.getApiToken()
        if (apiToken.isNullOrBlank()) {
            logger.e(TAG) { "No API Token provided, please ensure this is added to your build configuration" }
            return SessionState.Failed
        }

        // Attempt to create our new session. For now, we'll use our own API key and a default persona configuration. In
        // the future, we can extract this out to allow the caller to configure more information about their desired
        // experience.
        logger.i(TAG) { "Attempting to create new session..." }
        val result = client.createUnsafeSession(
            apiKey = apiToken,
            personaConfig = person.toConfig(),
            sessionOptions = SessionOptions(isLocalAudioEnabled = true),
        )

        return when (result) {
            is SessionResult.Success -> {
                val session = result.session
                activeSession.value = session
                isAudioMute.value = session.isLocalAudioMuted

                logger.i(TAG) { "Session created successfully: ${session.id}" }
                actionSessionJobs += coroutineScope.launch {
                    session.start()
                }

                // As well as starting the session, we should observe when it's disconnected. This could happen from
                // ourselves, but also from a server side termination.
                actionSessionJobs += coroutineScope.launch {
                    session.events.collect { event ->
                        if (event is SessionEvent.ConnectionClosed) {
                            cleanupSession()
                        }
                    }
                }

                SessionState.Success
            }

            is SessionResult.Error -> {
                logger.e(TAG, result.cause) { "Session failed to be created: ${result.message}" }
                SessionState.Failed
            }
        }
    }

    fun stopSession(): SessionState {
        val session = activeSession.value
        if (session != null) {
            logger.i(TAG) { "Stopping Session: ${session.id}" }
            cleanupSession()
        }

        return SessionState.Success
    }

    private fun cleanupSession() {
        if (activeSession.value == null) return

        activeSession.value = null
        isAudioMute.value = false
        actionSessionJobs.forEach { it.cancel() }
        actionSessionJobs.clear()
    }

    suspend fun sendUserMessage(content: String) {
        val text = content.trim()
        if (text.isEmpty()) return

        val session = activeSession.value
        if (session == null) {
            logger.i(TAG) { "No active session; ignoring sendUserMessage call." }
            return
        }

        logger.i(TAG) { "Sending user message: $text" }
        session.sendUserMessage(text)
    }

    fun toggleAudioMute() {
        val session = activeSession.value ?: return

        val isMute = isAudioMute.value
        if (isMute) {
            logger.i(TAG) { "Unmuting local audio" }
            session.setLocalAudioMuted(false)
            isAudioMute.value = false
        } else {
            logger.i(TAG) { "Muting local audio" }
            session.setLocalAudioMuted(true)
            isAudioMute.value = true
        }
    }

    /**
     * Extension function to convert our [Persona] model into the [PersonaConfig] required by the SDK.
     */
    private fun Persona.toConfig() = PersonaConfig(
        name = name,
        avatarId = avatarId,
        voiceId = voiceId,
        llmId = llmId,
        systemPrompt = systemPrompt,
        maxSessionLengthSeconds = maxSessionLengthSeconds,
    )

    private companion object {
        const val TAG = "SessionRepository"
    }
}
