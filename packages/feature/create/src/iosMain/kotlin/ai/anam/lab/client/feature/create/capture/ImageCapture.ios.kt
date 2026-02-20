package ai.anam.lab.client.feature.create.capture

import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_camera_permission_required
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_switch_camera_content_description
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_take_photo_content_description
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.ui.camera.CameraMode
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun CameraPreview(onCapture: (ByteArray) -> Unit, modifier: Modifier) {
    val cameraState = rememberPeekabooCameraState(
        initialCameraMode = CameraMode.Front,
        onCapture = { bytes ->
            if (bytes != null) {
                onCapture(bytes)
            }
        },
    )
    Box(modifier = modifier) {
        PeekabooCamera(
            state = cameraState,
            modifier = Modifier.fillMaxSize(),
            permissionDeniedContent = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.create_avatar_camera_permission_required))
                }
            },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            CameraActionButton(
                onClick = { cameraState.capture() },
                imageVector = Icons.Default.Camera,
                contentDescription = stringResource(Res.string.create_avatar_take_photo_content_description),
                buttonSize = 72.dp,
                iconSize = 36.dp,
            )
            CameraActionButton(
                onClick = { cameraState.toggleCamera() },
                enabled = cameraState.isCameraReady,
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = stringResource(Res.string.create_avatar_switch_camera_content_description),
                buttonSize = 48.dp,
                iconSize = 24.dp,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            )
        }
    }
}

@Composable
private fun CameraActionButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    buttonSize: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.8f)),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
actual fun rememberGalleryPickerLauncher(onImageSelect: (ByteArray) -> Unit): GalleryPickerState {
    val scope = rememberCoroutineScope()
    val picker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { images ->
            images.firstOrNull()?.let { onImageSelect(it) }
        },
    )
    return remember(picker) { GalleryPickerState(picker) }
}

actual class GalleryPickerState(private val picker: com.preat.peekaboo.image.picker.ImagePickerLauncher) {
    actual fun launch() {
        picker.launch()
    }
}
