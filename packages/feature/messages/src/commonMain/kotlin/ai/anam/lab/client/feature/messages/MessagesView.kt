package ai.anam.lab.client.feature.messages

import ai.anam.lab.Message
import ai.anam.lab.MessageRole
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.messages_empty_state_message
import ai.anam.lab.client.core.ui.resources.generated.resources.messages_send_content_description
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

private val SendGreen = Color(87, 190, 60)

@Composable
fun MessagesView(modifier: Modifier = Modifier, viewModel: MessagesViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    MessagesView(
        modifier = modifier,
        viewState = viewState,
        onSendUserMessage = viewModel::sendUserMessage,
    )
}

@Composable
fun MessagesView(
    viewState: MessagesViewState,
    onSendUserMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    fadeDurationMs: Int = 250,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = viewState.isActive,
            enter = fadeIn(animationSpec = tween(fadeDurationMs)),
            exit = fadeOut(animationSpec = tween(fadeDurationMs)),
        ) {
            MessagesContent(
                viewState = viewState,
                onSendUserMessage = onSendUserMessage,
            )
        }

        AnimatedVisibility(
            visible = !viewState.isActive,
            enter = fadeIn(animationSpec = tween(fadeDurationMs)),
            exit = fadeOut(animationSpec = tween(fadeDurationMs)),
        ) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(Res.string.messages_empty_state_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun MessagesContent(viewState: MessagesViewState, onSendUserMessage: (String) -> Unit) {
    val listState = rememberLazyListState()
    var isAutoScrolling by remember { mutableStateOf(false) }
    var hasScrolledUp by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

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

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = viewState.messages,
                key = { message -> "${message.id}::${message.version}" },
            ) { message ->
                ChatMessage(message)
            }
        }

        MessageInput(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    onSendUserMessage(inputText)
                    inputText = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
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

@Composable
private fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
        )

        FilledIconButton(
            onClick = onSend,
            enabled = value.isNotBlank(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = SendGreen,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(Res.string.messages_send_content_description),
                tint = Color.White,
            )
        }
    }
}
