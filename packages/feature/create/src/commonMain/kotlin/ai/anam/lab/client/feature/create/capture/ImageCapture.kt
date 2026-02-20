package ai.anam.lab.client.feature.create.capture

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreview(onCapture: (ByteArray) -> Unit, modifier: Modifier = Modifier)

@Composable
expect fun rememberGalleryPickerLauncher(onImageSelect: (ByteArray) -> Unit): GalleryPickerState

expect class GalleryPickerState {
    fun launch()
}
