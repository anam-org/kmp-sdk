package ai.anam.lab.ui

import WebRTC.RTCMTLVideoView
import WebRTC.RTCVideoRendererProtocol
import WebRTC.RTCVideoViewDelegateProtocol
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.shepeliev.webrtckmp.VideoTrack
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSize
import platform.UIKit.UIApplication
import platform.UIKit.UIViewContentMode
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun VideoSession(videoTrack: VideoTrack, onFirstFrameRendered: () -> Unit, modifier: Modifier) {
    // Create a delegate to detect when the first frame has been rendered.
    val frameDelegate = remember { FirstFrameDelegate(onFirstFrameRendered) }

    DisposableEffect(Unit) {
        val application = UIApplication.sharedApplication
        val previousIdleTimerDisabled = application.idleTimerDisabled
        application.idleTimerDisabled = true

        onDispose {
            application.idleTimerDisabled = previousIdleTimerDisabled
        }
    }

    UIKitView(
        factory = {
            RTCMTLVideoView().apply {
                videoContentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
                delegate = frameDelegate
                videoTrack.addRenderer(this)
            }
        },
        modifier = modifier,
        onRelease = { videoTrack.removeRenderer(it) },
    )
}

@OptIn(ExperimentalForeignApi::class)
private class FirstFrameDelegate(private val onFirstFrameRendered: () -> Unit) :
    NSObject(),
    RTCVideoViewDelegateProtocol {
    private var hasRenderedFirstFrame = false

    override fun videoView(videoView: RTCVideoRendererProtocol, didChangeVideoSize: CValue<CGSize>) {
        if (hasRenderedFirstFrame) return

        didChangeVideoSize.useContents {
            if (width > 0 && height > 0) {
                hasRenderedFirstFrame = true
                onFirstFrameRendered()
            }
        }
    }
}
