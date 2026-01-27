package ai.anam.lab

import ai.anam.lab.ui.VideoSession
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

@Composable
public fun AnamVideo(session: Session?, modifier: Modifier = Modifier, onSessionFirstRender: () -> Unit = {}) {
    val state = session?.tracks?.collectAsState(null)
    AnamVideo(
        tracks = state?.value,
        onFirstFrameRendered = {
            session?.onFirstFrameRendered()
            onSessionFirstRender()
        },
        modifier = modifier,
    )
}

@Composable
internal fun AnamVideo(tracks: SessionTracks?, onFirstFrameRendered: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        if (tracks != null) {
            VideoSession(
                tracks = tracks,
                onFirstFrameRendered = onFirstFrameRendered,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
