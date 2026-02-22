package ai.anam.lab.client.feature.llms

import ai.anam.lab.client.core.data.models.Llm
import ai.anam.lab.client.core.ui.components.PaginationErrorIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyColumn

@Composable
fun LlmsView(modifier: Modifier = Modifier, viewModel: LlmsViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    LlmsView(viewState = viewState, onLlmSelect = viewModel::setLlm, modifier = modifier)
}

@Composable
fun LlmsView(viewState: LlmsViewState, onLlmSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    PaginatedLazyColumn(
        paginationState = viewState.items,
        modifier = modifier.fillMaxWidth(),
        firstPageErrorIndicator = { exception ->
            PaginationErrorIndicator(
                exception = exception,
                onRetry = { viewState.items.retryLastFailedRequest() },
            )
        },
    ) {
        itemsIndexed(
            viewState.items.allItems!!,
        ) { _, item ->
            Llm(llm = item, isSelected = viewState.selectedId == item.id, onLlmSelect = onLlmSelect)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
fun Llm(llm: Llm, isSelected: Boolean, onLlmSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLlmSelect(llm.id) },
    ) {
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1F),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = llm.displayName,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )

            Text(
                text = llm.toSubtitle(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6F),
                style = MaterialTheme.typography.labelSmall,
            )

            val description = llm.description
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
