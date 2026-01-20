package ai.anam.lab.client.feature.messages

import ai.anam.lab.Message
import ai.anam.lab.MessageRole
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.messages_empty_state_message
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

@Composable
fun MessagesView(modifier: Modifier = Modifier, viewModel: MessagesViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    MessagesView(modifier = modifier, viewState = viewState)
}

@Composable
fun MessagesView(viewState: MessagesViewState, modifier: Modifier = Modifier) {
    if (viewState.isActive) {
        val listState = rememberLazyListState()
        var isAutoScrolling by remember { mutableStateOf(false) }
        var hasScrolledUp by remember { mutableStateOf(false) }

        // Detect when the user manually scrolls up. If they do, we stop auto-scrolling.
        // If they scroll back to the bottom, we resume auto-scrolling.
        LaunchedEffect(listState) {
            snapshotFlow { listState.canScrollForward }
                .collect { canScrollForward ->
                    if (listState.isScrollInProgress && !isAutoScrolling) {
                        hasScrolledUp = canScrollForward
                    }
                }
        }

        // Automatically scroll to the bottom when new messages arrive, unless the user
        // has manually scrolled up.
        LaunchedEffect(viewState.messages) {
            if (!hasScrolledUp && viewState.messages.isNotEmpty()) {
                isAutoScrolling = true
                listState.animateScrollToItem(viewState.messages.lastIndex)
                isAutoScrolling = false
            }
        }

        LazyColumn(
            state = listState,
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = viewState.messages,
                key = { message -> "${message.id}::${message.version}" },
            ) { message ->
                ChatMessage(message)
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            Text(
                text = stringResource(Res.string.messages_empty_state_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun ChatMessage(message: Message, modifier: Modifier = Modifier) {
    val isPersona = message.role == MessageRole.Persona

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isPersona) Arrangement.Start else Arrangement.End,
        ) {
            Box(
                modifier = Modifier
                    .widthIn()
                    .background(
                        color = if (isPersona) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        shape = RoundedCornerShape(8.dp),
                    ).padding(4.dp),
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = if (isPersona) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    },
                )
            }
        }
    }
}
