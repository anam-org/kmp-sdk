package ai.anam.lab.ui

import ai.anam.lab.SessionTracks
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun VideoSession(
    tracks: SessionTracks,
    onFirstFrameRendered: () -> Unit,
    modifier: Modifier = Modifier,
)
