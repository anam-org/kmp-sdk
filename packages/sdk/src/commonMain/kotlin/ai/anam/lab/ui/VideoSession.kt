package ai.anam.lab.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.VideoTrack

@Composable
internal expect fun VideoSession(
    videoTrack: VideoTrack,
    onFirstFrameRendered: () -> Unit,
    modifier: Modifier = Modifier,
)
