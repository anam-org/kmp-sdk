package ai.anam.lab.ui

import ai.anam.lab.SessionTracks
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import com.shepeliev.webrtckmp.MediaStream
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.MediaProvider

/**
 * wasmJs implementation of [VideoSession] following the webrtc-kmp wasmJs pattern:
 * https://github.com/shepeliev/webrtc-kmp/blob/main/sample/composeApp/src/wasmJsMain/kotlin/Video.wasmJs.kt
 *
 * Uses a [MediaStream], an `HTMLVideoElement` with `srcObject` set to the stream's underlying JS object,
 * and [Box] + [onGloballyPositioned] to position the video overlay. [onFirstFrameRendered] is invoked
 * when `HTMLVideoElement.onloadeddata` fires.
 */
@Composable
internal actual fun VideoSession(tracks: SessionTracks, onFirstFrameRendered: () -> Unit, modifier: Modifier) {
    val stream = remember { MediaStream() }

    val videoElement =
        remember {
            (document.createElement("video") as HTMLVideoElement).apply {
                srcObject = stream.js as MediaProvider
                autoplay = true
                style.position = "absolute"
                style.objectFit = "cover"
            }
        }

    DisposableEffect(videoElement, stream) {
        document.body?.appendChild(videoElement)
        onDispose {
            document.body?.removeChild(videoElement)
            videoElement.srcObject = null
            stream.release()
        }
    }

    val onFirstFrameState = rememberUpdatedState(onFirstFrameRendered)
    DisposableEffect(tracks.videoTrack) {
        stream.addTrack(tracks.videoTrack)
        videoElement.onloadeddata = { onFirstFrameState.value() }
        onDispose {
            videoElement.onloadeddata = null
            stream.removeTrack(tracks.videoTrack)
        }
    }

    DisposableEffect(tracks.audioTrack) {
        stream.addTrack(tracks.audioTrack)
        onDispose {
            stream.removeTrack(tracks.audioTrack)
        }
    }

    val density = LocalDensity.current

    Box(
        modifier =
        modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                with(density) {
                    with(videoElement.style) {
                        top = "${coordinates.positionInWindow().y.toDp().value}px"
                        left = "${coordinates.positionInWindow().x.toDp().value}px"
                        width = "${coordinates.size.width.toDp().value}px"
                        height = "${coordinates.size.height.toDp().value}px"
                    }
                }
            },
    )
}
