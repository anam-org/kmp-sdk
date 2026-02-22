package ai.anam.lab.client.feature.session

import ai.anam.lab.ConnectionClosedReason
import ai.anam.lab.Session
import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.notifications.ErrorCode
import ai.anam.lab.client.core.notifications.Notification
import ai.anam.lab.client.core.permissions.PermissionResult
import ai.anam.lab.client.core.permissions.PermissionsManager
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.ObserveCurrentAvatarInteractor
import ai.anam.lab.client.domain.notifications.SendNotificationInteractor
import ai.anam.lab.client.domain.permissions.RequestAudioPermissionInteractor
import ai.anam.lab.client.domain.session.ObserveActiveSessionClosedInteractor
import ai.anam.lab.client.domain.session.ObserveActiveSessionInteractor
import ai.anam.lab.client.domain.session.ObserveActiveSessionMuteStateInteractor
import ai.anam.lab.client.domain.session.StartSessionWithCurrentPersonaInteractor
import ai.anam.lab.client.domain.session.StopSessionInteractor
import ai.anam.lab.client.domain.session.ToggleActiveSessionMuteStateInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch

@Inject
class SessionViewModel(
    private val observeActiveSessionInteractor: ObserveActiveSessionInteractor,
    private val observeCurrentAvatarInteractor: ObserveCurrentAvatarInteractor,
    private val observeActiveSessionClosedInteractor: ObserveActiveSessionClosedInteractor,
    private val startSessionInteractor: StartSessionWithCurrentPersonaInteractor,
    private val stopSessionInteractor: StopSessionInteractor,
    private val observeActiveSessionMuteStateInteractor: ObserveActiveSessionMuteStateInteractor,
    private val toggleActiveSessionMuteStateInteractor: ToggleActiveSessionMuteStateInteractor,
    private val sendNotificationInteractor: SendNotificationInteractor,
    permissionsManager: PermissionsManager,
    private val requestAudioPermissionInteractor: RequestAudioPermissionInteractor,
    private val logger: Logger,
) : BaseViewModel<SessionViewState>(SessionViewState(permissionsManager = permissionsManager)) {

    init {
        viewModelScope.launch {
            observeActiveSessionInteractor().collect { session ->
                logger.i(TAG) { "Session change detected: ${session?.id}" }
                val sessionState = if (session == null) {
                    SessionState.None
                } else {
                    SessionState.Started(session)
                }
                setState { copy(sessionState = sessionState) }
            }
        }

        viewModelScope.launch {
            observeCurrentAvatarInteractor().collect { result ->
                result.onRight { avatar ->
                    logger.i(TAG) { "Fetched avatar: $avatar" }
                    setState {
                        copy(
                            imageUrl = avatar.imageUrl,
                            videoUrl = avatar.videoUrl,
                            isControlEnabled = true,
                        )
                    }
                }.onLeft { error ->
                    logger.e(TAG) { "Failed to fetch avatar: $error" }
                    setState {
                        copy(
                            imageUrl = null,
                            videoUrl = null,
                            isControlEnabled = false,
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            observeActiveSessionMuteStateInteractor().collect { isMute ->
                setState { copy(isAudioMute = isMute) }
            }
        }

        viewModelScope.launch {
            observeActiveSessionClosedInteractor()
                .filterNot { event -> event is ConnectionClosedReason.Normal }
                .collect { event ->
                    val message = when (event) {
                        is ConnectionClosedReason.ServerConnectionClosed -> event.reason
                        is ConnectionClosedReason.WebRtcFailure -> event.message
                        is ConnectionClosedReason.SignallingClientConnectionFailure -> event.message
                        else -> null
                    }

                    sendNotificationInteractor(
                        Notification.Error(
                            errorCode = ErrorCode.SDK_ERROR,
                            customMessage = message,
                        ),
                    )
                }
        }
    }

    fun startSession() {
        logger.i(TAG) { "Starting new session..." }
        viewModelScope.launch {
            val permission = requestAudioPermissionInteractor()
            if (permission != PermissionResult.Granted) {
                logger.e(TAG) { "Audio permission not granted: $permission" }
                return@launch
            }

            setState { copy(sessionState = SessionState.Loading) }
            startSessionInteractor()
        }
    }

    fun stopSession() {
        logger.i(TAG) { "Stopping session" }
        setState { copy(sessionState = SessionState.None) }

        viewModelScope.launch {
            stopSessionInteractor()
        }
    }

    fun toggleAudioMute() {
        logger.i(TAG) { "Toggling audio mute" }
        viewModelScope.launch {
            toggleActiveSessionMuteStateInteractor()
        }
    }

    private companion object {
        const val TAG = "SessionViewModel"
    }
}

data class SessionViewState(
    val permissionsManager: PermissionsManager,
    val sessionState: SessionState = SessionState.None,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val isAudioMute: Boolean = false,
    val isControlEnabled: Boolean = false,
) : ViewState

sealed interface SessionState {
    data object None : SessionState
    data object Loading : SessionState
    data class Started(val session: Session) : SessionState
}
