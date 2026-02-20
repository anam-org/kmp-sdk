package ai.anam.lab.client.feature.create

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.compression.compressImage
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.permissions.PermissionResult
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.CreateAvatarInteractor
import ai.anam.lab.client.domain.permissions.RequestCameraPermissionInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
class CreateAvatarViewModel(
    private val createAvatarInteractor: CreateAvatarInteractor,
    private val requestCameraPermissionInteractor: RequestCameraPermissionInteractor,
    private val navigator: Navigator,
    private val logger: Logger,
) : BaseViewModel<CreateAvatarViewState>(CreateAvatarViewState()) {

    init {
        viewModelScope.launch {
            logger.i(TAG) { "Requesting camera permission" }
            val result = requestCameraPermissionInteractor()
            logger.i(TAG) { "Camera permission result: $result" }
            setState { copy(cameraPermission = result) }
        }
    }

    fun onImageCaptured(imageData: ByteArray) {
        logger.i(TAG) { "Image captured: ${imageData.size} bytes" }
        viewModelScope.launch {
            val compressed = compressImage(imageData)
            logger.i(TAG) { "Image compressed: ${imageData.size} -> ${compressed.size} bytes" }
            setState { copy(step = CreateAvatarStep.Review, imageData = ImageData(compressed)) }
        }
    }

    fun onDisplayNameChange(displayName: String) {
        if (displayName.length <= DISPLAY_NAME_MAX_LENGTH) {
            setState { copy(displayName = displayName) }
        }
    }

    fun onSubmit() {
        val currentState = state.value
        val imageData = currentState.imageData?.bytes ?: return
        val displayName = currentState.displayName.trim()
        if (displayName.length < DISPLAY_NAME_MIN_LENGTH) return

        setState { copy(step = CreateAvatarStep.Uploading, error = null) }

        viewModelScope.launch {
            logger.i(TAG) { "Submitting avatar: $displayName" }
            when (val result = createAvatarInteractor(displayName, imageData)) {
                is Either.Right -> {
                    logger.i(TAG) { "Avatar created: ${result.value.id}" }
                    setState {
                        copy(
                            step = CreateAvatarStep.Success,
                            createdAvatarName = result.value.displayName,
                        )
                    }
                }
                is Either.Left -> {
                    logger.e(TAG) { "Failed to create avatar: ${result.value}" }
                    setState {
                        copy(
                            step = CreateAvatarStep.Error,
                            error = "Failed to create avatar. Please try again.",
                        )
                    }
                }
            }
        }
    }

    fun onRetry() {
        setState { copy(step = CreateAvatarStep.Review, error = null) }
    }

    fun onNavigateBack() {
        navigator.pop()
    }

    private companion object {
        const val TAG = "CreateAvatarViewModel"
        const val DISPLAY_NAME_MIN_LENGTH = 3
        const val DISPLAY_NAME_MAX_LENGTH = 50
    }
}

enum class CreateAvatarStep {
    Capture,
    Review,
    Uploading,
    Success,
    Error,
}

data class CreateAvatarViewState(
    val step: CreateAvatarStep = CreateAvatarStep.Capture,
    val cameraPermission: PermissionResult? = null,
    val imageData: ImageData? = null,
    val displayName: String = "",
    val createdAvatarName: String? = null,
    val error: String? = null,
) : ViewState {
    val isSubmitEnabled: Boolean
        get() = displayName.trim().length >= 3 && imageData != null
}
