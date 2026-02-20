package ai.anam.lab.client.feature.create

import ai.anam.lab.client.core.navigation.LocalAnimatedVisibilityScope
import ai.anam.lab.client.core.permissions.PermissionResult
import ai.anam.lab.client.core.ui.core.ImmersiveMode
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.app_name
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_back_content_description
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_camera_permission_denied
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_done_button
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_error_title
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_go_back_button
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_image_captured
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_name_hint
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_name_label
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_name_placeholder
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_one_shot_avatar
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_pick_from_gallery
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_retry_button
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_success_title
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_uploading
import ai.anam.lab.client.core.viewmodel.metroViewModel
import ai.anam.lab.client.feature.create.capture.CameraPreview
import ai.anam.lab.client.feature.create.capture.rememberGalleryPickerLauncher
import androidx.compose.animation.EnterExitState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateAvatarScreen(modifier: Modifier = Modifier, viewModel: CreateAvatarViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val isTransitionComplete = animatedVisibilityScope?.transition?.currentState == EnterExitState.Visible
    CreateAvatarScreen(
        viewState = viewState,
        isTransitionComplete = isTransitionComplete,
        onCapture = viewModel::onImageCaptured,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onSubmit = viewModel::onSubmit,
        onRetry = viewModel::onRetry,
        onNavigateBack = viewModel::onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAvatarScreen(
    viewState: CreateAvatarViewState,
    isTransitionComplete: Boolean,
    onCapture: (ByteArray) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCapture = viewState.step == CreateAvatarStep.Capture

    ImmersiveMode(enabled = isCapture)

    if (isCapture) {
        CaptureStep(
            cameraPermission = viewState.cameraPermission,
            isTransitionComplete = isTransitionComplete,
            onCapture = onCapture,
            onNavigateBack = onNavigateBack,
            modifier = modifier.fillMaxSize(),
        )
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.app_name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.create_avatar_back_content_description),
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                when (viewState.step) {
                    CreateAvatarStep.Capture -> { /* handled above */ }
                    CreateAvatarStep.Review -> ReviewStep(
                        imageData = viewState.imageData,
                        displayName = viewState.displayName,
                        isSubmitEnabled = viewState.isSubmitEnabled,
                        onDisplayNameChange = onDisplayNameChange,
                        onSubmit = onSubmit,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                    )
                    CreateAvatarStep.Uploading -> UploadingStep(
                        modifier = Modifier.fillMaxSize(),
                    )
                    CreateAvatarStep.Success -> SuccessStep(
                        avatarName = viewState.createdAvatarName.orEmpty(),
                        onDone = onNavigateBack,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                    )
                    CreateAvatarStep.Error -> ErrorStep(
                        error = viewState.error.orEmpty(),
                        onRetry = onRetry,
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureStep(
    cameraPermission: PermissionResult?,
    isTransitionComplete: Boolean,
    onCapture: (ByteArray) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val galleryPicker = rememberGalleryPickerLauncher(onImageSelect = onCapture)

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            when {
                !isTransitionComplete || cameraPermission == null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                cameraPermission == PermissionResult.Granted -> {
                    CameraPreview(
                        onCapture = onCapture,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.create_avatar_camera_permission_denied),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { galleryPicker.launch() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(Res.string.create_avatar_pick_from_gallery))
                }
            }
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f)),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.create_avatar_back_content_description),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun ReviewStep(
    imageData: ImageData?,
    displayName: String,
    isSubmitEnabled: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = imageData?.bytes,
            contentDescription = stringResource(Res.string.create_avatar_image_captured),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp)),
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            label = { Text(stringResource(Res.string.create_avatar_name_label)) },
            placeholder = { Text(stringResource(Res.string.create_avatar_name_placeholder)) },
            supportingText = { Text(stringResource(Res.string.create_avatar_name_hint)) },
            trailingIcon = {
                if (displayName.isNotEmpty()) {
                    IconButton(onClick = { onDisplayNameChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onSubmit,
            enabled = isSubmitEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.create_avatar_one_shot_avatar))
        }
    }
}

@Composable
private fun UploadingStep(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(stringResource(Res.string.create_avatar_uploading))
        }
    }
}

@Composable
private fun SuccessStep(avatarName: String, onDone: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.create_avatar_success_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = avatarName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = onDone) {
            Text(stringResource(Res.string.create_avatar_done_button))
        }
    }
}

@Composable
private fun ErrorStep(error: String, onRetry: () -> Unit, onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.create_avatar_error_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text(stringResource(Res.string.create_avatar_retry_button))
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(onClick = onNavigateBack) {
            Text(stringResource(Res.string.create_avatar_go_back_button))
        }
    }
}
