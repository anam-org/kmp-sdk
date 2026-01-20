package ai.anam.lab.client.feature.session

import ai.anam.lab.Session
import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.permissions.PermissionResult
import ai.anam.lab.client.core.permissions.PermissionsManager
import ai.anam.lab.client.domain.data.ObserveCurrentAvatarInteractor
import ai.anam.lab.client.domain.permissions.RequestAudioPermissionInteractor
import ai.anam.lab.client.domain.session.ObserveActiveSessionInteractor
import ai.anam.lab.client.domain.session.ObserveActiveSessionMuteStateInteractor
import ai.anam.lab.client.domain.session.StartSessionWithCurrentPersonaInteractor
import ai.anam.lab.client.domain.session.StopSessionInteractor
import ai.anam.lab.client.domain.session.ToggleActiveSessionMuteStateInteractor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
@ViewModelKey(SessionViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class SessionViewModel(
    private val observeActiveSessionInteractor: ObserveActiveSessionInteractor,
    private val observeCurrentAvatarInteractor: ObserveCurrentAvatarInteractor,
    private val startSessionInteractor: StartSessionWithCurrentPersonaInteractor,
    private val stopSessionInteractor: StopSessionInteractor,
    private val observeActiveSessionMuteStateInteractor: ObserveActiveSessionMuteStateInteractor,
    private val toggleActiveSessionMuteStateInteractor: ToggleActiveSessionMuteStateInteractor,
    private val permissionsManager: PermissionsManager,
    private val requestAudioPermissionInteractor: RequestAudioPermissionInteractor,
    private val logger: Logger,
) : ViewModel() {

    private val _state = MutableStateFlow(SessionViewState(permissionsManager = permissionsManager))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeActiveSessionInteractor().collect { session ->
                logger.i(TAG) { "Session change detected: ${session?.id}" }
                val state = if (session == null) {
                    SessionState.None
                } else {
                    SessionState.Started(session)
                }
                val newState = _state.value.copy(
                    sessionState = state,
                )

                _state.value = newState
            }
        }

        viewModelScope.launch {
            observeCurrentAvatarInteractor().collect { result ->
                result.onRight { avatar ->
                    logger.i(TAG) { "Fetched avatar: $avatar" }
                    _state.value = _state.value.copy(
                        imageUrl = avatar.imageUrl,
                        videoUrl = avatar.videoUrl,
                    )
                }.onLeft { error ->
                    logger.e(TAG) { "Failed to fetch avatar: $error" }
                }
            }
        }

        viewModelScope.launch {
            observeActiveSessionMuteStateInteractor().collect { isMute ->
                _state.value = _state.value.copy(isAudioMute = isMute)
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

            val newState = _state.value.copy(sessionState = SessionState.Loading)
            _state.value = newState
            startSessionInteractor()
        }
    }

    fun stopSession() {
        logger.i(TAG) { "Stopping session" }
        val newState = _state.value.copy(sessionState = SessionState.None)
        _state.value = newState

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
)

sealed interface SessionState {
    data object None : SessionState
    data object Loading : SessionState
    data class Started(val session: Session) : SessionState
}
