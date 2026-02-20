package ai.anam.lab.client.core.permissions.di

import ai.anam.lab.client.core.permissions.PermissionResult
import ai.anam.lab.client.core.permissions.PlatformPermissionsController
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Invokes [navigator.mediaDevices.getUserMedia](https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia)
 * with `{ audio: true }`. On success, stops all tracks to release the device, then calls [onResult](true).
 * On rejection or if [mediaDevices] is unavailable, calls [onResult](false).
 * Must be top-level for Kotlin/Wasm [js()] interop.
 */
private fun requestAudioFromBrowser(onResult: (Boolean) -> Unit): Unit = js(
    """
        {
          if (!window.navigator.mediaDevices) {
            onResult(false);
            return;
          }
          window.navigator.mediaDevices.getUserMedia({ audio: true })
            .then(function(stream) {
              try {
                stream.getTracks().forEach(function(t) { t.stop(); });
              } catch (e) {}
              onResult(true);
            })
            .catch(function() { onResult(false); });
        }
        """,
)

/**
 * Invokes [navigator.mediaDevices.getUserMedia](https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia)
 * with `{ video: true }`. On success, stops all tracks to release the device, then calls [onResult](true).
 * On rejection or if [mediaDevices] is unavailable, calls [onResult](false).
 * Must be top-level for Kotlin/Wasm [js()] interop.
 */
private fun requestVideoFromBrowser(onResult: (Boolean) -> Unit): Unit = js(
    """
        {
          if (!window.navigator.mediaDevices) {
            onResult(false);
            return;
          }
          window.navigator.mediaDevices.getUserMedia({ video: true })
            .then(function(stream) {
              try {
                stream.getTracks().forEach(function(t) { t.stop(); });
              } catch (e) {}
              onResult(true);
            })
            .catch(function() { onResult(false); });
        }
        """,
)

/**
 * wasmJs implementation using browser [navigator.mediaDevices.getUserMedia].
 * Requests microphone/camera access; on success stops all tracks immediately to release the device.
 * [DeniedAlways] cannot be distinguished from [Denied] in the browser, so rejections map to [Denied].
 */
internal class BrowserPlatformPermissionsController : PlatformPermissionsController {

    override val bindTarget: Any? get() = null

    override suspend fun requestRecordAudio(): PermissionResult = suspendCancellableCoroutine { cont ->
        requestAudioFromBrowser { granted ->
            cont.resume(if (granted) PermissionResult.Granted else PermissionResult.Denied)
        }
    }

    override suspend fun requestCamera(): PermissionResult = suspendCancellableCoroutine { cont ->
        requestVideoFromBrowser { granted ->
            cont.resume(if (granted) PermissionResult.Granted else PermissionResult.Denied)
        }
    }
}
