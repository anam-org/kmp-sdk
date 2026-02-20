package ai.anam.lab.client.feature.create.capture

import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_camera_not_supported_web
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray

@Composable
actual fun CameraPreview(onCapture: (ByteArray) -> Unit, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(Res.string.create_avatar_camera_not_supported_web))
    }
}

@Composable
actual fun rememberGalleryPickerLauncher(onImageSelect: (ByteArray) -> Unit): GalleryPickerState {
    return remember(onImageSelect) { GalleryPickerState(onImageSelect) }
}

actual class GalleryPickerState(private val onImageSelect: (ByteArray) -> Unit) {
    actual fun launch() {
        launchFilePicker { arrayBuffer ->
            val bytes = Int8Array(arrayBuffer).toByteArray()
            onImageSelect(bytes)
        }
    }
}

private fun launchFilePicker(onResult: (ArrayBuffer) -> Unit) {
    val callback: (JsAny) -> Unit = { jsArrayBuffer ->
        @Suppress("UNCHECKED_CAST")
        onResult(jsArrayBuffer as ArrayBuffer)
    }
    launchFilePickerJs(callback)
}

private fun launchFilePickerJs(callback: (JsAny) -> Unit): Unit = js(
    """{
        var input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/*';
        input.onchange = function(e) {
            var file = e.target.files[0];
            if (file) {
                var reader = new FileReader();
                reader.onload = function(ev) {
                    callback(ev.target.result);
                };
                reader.readAsArrayBuffer(file);
            }
        };
        input.click();
    }""",
)
