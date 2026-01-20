package ai.anam.lab.client.feature.session

import ai.anam.lab.AnamVideo
import ai.anam.lab.client.core.permissions.BindEffect
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.session_call_content_description
import ai.anam.lab.client.core.ui.resources.generated.resources.session_hangup_content_description
import ai.anam.lab.client.core.ui.video.AvatarVideo
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource

@Composable
fun SessionView(modifier: Modifier = Modifier, viewModel: SessionViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    SessionView(
        modifier = modifier,
        viewState = viewState,
        onStartSession = viewModel::startSession,
        onStopSession = viewModel::stopSession,
        onToggleAudioMute = viewModel::toggleAudioMute,
    )
}

@Composable
fun SessionView(
    viewState: SessionViewState,
    onStartSession: () -> Unit,
    onStopSession: () -> Unit,
    onToggleAudioMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BindEffect(viewState.permissionsManager)

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondary),
    ) {
        // Record when the video preview has been started.
        var isPreviewVideoStarted by remember(viewState.sessionState, viewState.videoUrl) {
            mutableStateOf(false)
        }

        // Record when the actual session has started (rendering).
        var isSessionStarted by remember(viewState.sessionState) { mutableStateOf(false) }

        val isIdle = viewState.sessionState == SessionState.None
        val isPreviewLoading = isIdle && !isPreviewVideoStarted
        val isSessionLoading = viewState.sessionState == SessionState.Loading ||
            (viewState.sessionState is SessionState.Started && !isSessionStarted)

        if (isIdle && viewState.videoUrl != null) {
            AvatarVideo(
                url = viewState.videoUrl,
                backgroundColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxSize(),
                onPlayerStart = { isPreviewVideoStarted = true },
            )
        }

        val session = (viewState.sessionState as? SessionState.Started)?.session
        if (session != null) {
            AnamVideo(
                session = session,
                modifier = Modifier.fillMaxSize(),
                onSessionFirstRender = {
                    isSessionStarted = true
                },
            )
        }

        // Display a Shutter over either the video preview, or the session, if either of these are considered "loading".
        // This will help us avoid displaying a blank image until we're ready to display something useful.
        ShutterView(
            imageUrl = viewState.imageUrl,
            isPreviewLoading = isPreviewLoading,
            isSessionLoading = isSessionLoading,
            modifier = Modifier.fillMaxSize(),
        )

        SessionControls(
            isSessionActive = !isIdle,
            isAudioMute = viewState.isAudioMute,
            onStartSession = onStartSession,
            onStopSession = onStopSession,
            onToggleAudioMute = onToggleAudioMute,
        )
    }
}

private val CallGreen = Color(87, 190, 60)
private val HangupRed = Color(229, 83, 68)

@Composable
fun ShutterView(
    imageUrl: String?,
    isPreviewLoading: Boolean,
    isSessionLoading: Boolean,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 20.dp,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isPreviewLoading || isSessionLoading,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(blurRadius),
            )

            if (isSessionLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    }
}

@Composable
fun SessionControls(
    isSessionActive: Boolean,
    isAudioMute: Boolean,
    onStartSession: () -> Unit,
    onStopSession: () -> Unit,
    onToggleAudioMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        AnimatedVisibility(
            visible = isSessionActive,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopEnd),
        ) {
            MuteButton(
                isMuted = isAudioMute,
                onClick = onToggleAudioMute,
                modifier = Modifier.fillMaxSize(),
            )
        }

        SessionControlButton(
            isSessionActive = isSessionActive,
            onClick = if (isSessionActive) onStopSession else onStartSession,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd),
        )
    }
}

@Composable
fun SessionControlButton(
    isSessionActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    duration: Int = 500,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSessionActive) HangupRed else CallGreen,
        animationSpec = tween(durationMillis = duration),
        label = "BackgroundColor",
    )
    val rotation by animateFloatAsState(
        targetValue = if (isSessionActive) 0f else -135f,
        animationSpec = tween(durationMillis = duration),
        label = "Rotation",
    )

    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = backgroundColor),
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Rounded.CallEnd,
            contentDescription = if (isSessionActive) {
                stringResource(Res.string.session_hangup_content_description)
            } else {
                stringResource(Res.string.session_call_content_description)
            },
            tint = Color.White,
            modifier = Modifier.rotate(rotation),
        )
    }
}

@Composable
fun MuteButton(isMuted: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isMuted) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surface.copy(
                alpha = 0.5f,
            )
        },
        label = "MuteButtonBackground",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isMuted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
        label = "MuteButtonContent",
    )

    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        modifier = modifier,
    ) {
        Icon(
            imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
            contentDescription = if (isMuted) "Unmute" else "Mute",
        )
    }
}
