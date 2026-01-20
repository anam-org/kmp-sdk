package ai.anam.lab.client.feature.voices

import ai.anam.lab.client.core.data.models.Voice
import ai.anam.lab.client.core.ui.components.PlayPauseButton
import ai.anam.lab.client.core.ui.components.SelectedBadge
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.ui.audio.AudioPlayer
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyColumn

@Composable
fun VoicesView(modifier: Modifier = Modifier, viewModel: VoicesViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    VoicesView(viewState = viewState, onVoiceSelect = viewModel::setVoice, modifier = modifier)
}

@Composable
fun VoicesView(viewState: VoicesViewState, onVoiceSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    PaginatedLazyColumn(
        paginationState = viewState.items,
        modifier = modifier.fillMaxWidth(),
    ) {
        itemsIndexed(
            viewState.items.allItems!!,
        ) { _, item ->
            Voice(voice = item, isSelected = viewState.selectedId == item.id, onVoiceSelect = onVoiceSelect)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
fun Voice(voice: Voice, isSelected: Boolean, onVoiceSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onVoiceSelect(voice.id) },
    ) {
        VoicePreview(
            sampleUrl = voice.sampleUrl,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Top),
        )

        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1F),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = voice.displayName,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )

            Text(
                text = voice.toSubtitle(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6F),
                style = MaterialTheme.typography.labelSmall,
            )

            val description = voice.description
            if (description != null) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6F),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        SelectedBadge(
            modifier = Modifier
                .size(24.dp)
                .alpha(
                    if (isSelected) {
                        1F
                    } else {
                        0F
                    },
                ),
        )
    }
}

@Composable
fun VoicePreview(sampleUrl: String?, modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(false) }

    // When we can't play something, let's make it look disabled by reducing the alpha.
    val color = if (sampleUrl == null) {
        MaterialTheme.colorScheme.primary.copy(0.2F)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val onClick: (() -> Unit)?
    if (sampleUrl != null) {
        val playerHost = remember {
            MediaPlayerHost(
                mediaUrl = sampleUrl,
                isPaused = true,
                isLooping = false,
            )
        }

        // Detect when playback reaches the end of the sample, so we can update our currently playing state.
        playerHost.onEvent = { event ->
            when (event) {
                is MediaPlayerEvent.MediaEnd -> {
                    playerHost.seekTo(0F)
                    playerHost.pause()
                    isPlaying = false
                }

                else -> Unit
            }
        }

        // For any error, we'll just revert back to our non-playing state.
        playerHost.onError = { _ ->
            isPlaying = false
        }

        AudioPlayer(playerHost = playerHost)

        // Build a Click handler that will toggle playback of the audio clip.
        onClick = {
            if (isPlaying) {
                isPlaying = false
                playerHost.pause()
                playerHost.seekTo(0F)
            } else {
                isPlaying = true
                playerHost.seekTo(0F)
                playerHost.play()
            }
        }
    } else {
        // When we don't have a sample, we avoid building a Callback. This will ensure the button isn't Clickable.
        onClick = null
    }

    PlayPauseButton(
        isPlaying = isPlaying,
        onClick = onClick,
        color = color,
        modifier = modifier,
    )
}
