package ai.anam.lab

import ai.anam.lab.ui.VideoSession
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.VideoTrack

@Composable
public fun AnamVideo(session: Session?, modifier: Modifier = Modifier, onSessionFirstRender: () -> Unit = {}) {
    val state = session?.remoteVideoTrack?.collectAsState(null)
    AnamVideo(
        videoTrack = state?.value,
        onFirstFrameRendered = {
            session?.onFirstFrameRendered()
            onSessionFirstRender()
        },
        modifier = modifier,
    )
}

@Composable
internal fun AnamVideo(videoTrack: VideoTrack?, onFirstFrameRendered: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        if (videoTrack != null) {
            VideoSession(
                videoTrack = videoTrack,
                onFirstFrameRendered = onFirstFrameRendered,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
