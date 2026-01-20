package ai.anam.lab.client.core.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable

@Composable
fun AvatarVideo(
    url: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black,
    onPlayerStart: () -> Unit = { },
    onPlayerError: () -> Unit = { },
) {
    var isStarted by remember(url) { mutableStateOf(false) }
    val playerHost = remember(url) {
        MediaPlayerHost(
            mediaUrl = url,
            isLooping = true,
        )
    }

    // Attach to detect any error's which occur during playback.
    playerHost.onError = { _ -> onPlayerError() }

    VideoPlayerComposable(
        modifier = modifier.fillMaxSize(),
        playerHost = playerHost,
        playerConfig = VideoPlayerConfig(
            showControls = false,
            isZoomEnabled = false,
            isGestureVolumeControlEnabled = false,
            isScreenResizeEnabled = false,
            isSpeedControlEnabled = false,
            isMuteControlEnabled = false,
            enablePIPControl = false,
            loadingIndicatorColor = Color.Transparent,
            loaderView = {
                // We only show the loader if the player hasn't started yet. This avoids it being displayed when looping
                if (!isStarted) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor),
                    )

                    // The MediaPlayerHost doesn't appear to support a way to detect when playback is starting. We will
                    // therefore try to detect when our custom LoaderView is disposed, which should be roughly
                    // equivalent.
                    DisposableEffect(onPlayerStart) {
                        onDispose {
                            isStarted = true
                            onPlayerStart()
                        }
                    }
                }
            },
        ),
    )
}
