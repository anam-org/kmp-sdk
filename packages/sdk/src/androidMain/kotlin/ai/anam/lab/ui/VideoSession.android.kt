package ai.anam.lab.ui

import ai.anam.lab.SessionTracks
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shepeliev.webrtckmp.VideoTrack
import com.shepeliev.webrtckmp.WebRtc
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink

/**
 * This Composable is taken from the KMP/WebRTC example.
 *
 * https://github.com/shepeliev/webrtc-kmp/blob/main/sample/composeApp/src/androidMain/kotlin/Video.android.kt
 */
@Composable
internal actual fun VideoSession(tracks: SessionTracks, onFirstFrameRendered: () -> Unit, modifier: Modifier) {
    var renderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    val lifecycleEventObserver =
        remember(renderer, tracks.videoTrack) {
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        renderer?.also {
                            it.init(
                                WebRtc.rootEglBase.eglBaseContext,
                                object : RendererCommon.RendererEvents {
                                    override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) = Unit

                                    override fun onFirstFrameRendered() {
                                        // Report that the first frame was rendered.
                                        onFirstFrameRendered()
                                    }
                                },
                            )

                            tracks.videoTrack.addSinkCatching(it)
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        renderer?.also { tracks.videoTrack.removeSinkCatching(it) }
                        renderer?.release()
                    }

                    else -> {
                        // ignore other events
                    }
                }
            }
        }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, lifecycleEventObserver) {
        lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            renderer?.let { tracks.videoTrack.removeSinkCatching(it) }
            renderer?.release()
            lifecycle.removeObserver(lifecycleEventObserver)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                setScalingType(
                    RendererCommon.ScalingType.SCALE_ASPECT_BALANCED,
                    RendererCommon.ScalingType.SCALE_ASPECT_FIT,
                )
                keepScreenOn = true
                renderer = this
            }
        },
    )
}

private fun VideoTrack.addSinkCatching(sink: VideoSink) {
    // runCatching as track may be disposed while activity was in pause
    runCatching { addSink(sink) }
}

private fun VideoTrack.removeSinkCatching(sink: VideoSink) {
    // runCatching as track may be disposed while activity was in pause
    runCatching { removeSink(sink) }
}
