package ai.anam.lab.client.feature.avatars

import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.datetime.toFormattedDateString
import ai.anam.lab.client.core.ui.components.SelectedBadge
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyVerticalGrid
import kotlin.time.ExperimentalTime

@Composable
fun AvatarsView(modifier: Modifier = Modifier, viewModel: AvatarsViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    AvatarsView(
        viewState = viewState,
        onAvatarSelect = viewModel::setAvatar,
        modifier = modifier,
    )
}

@Composable
fun AvatarsView(
    viewState: AvatarsViewState,
    onAvatarSelect: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Fixed(2),
) {
    PaginatedLazyVerticalGrid(
        paginationState = viewState.items,
        modifier = modifier.fillMaxSize(),
        columns = columns,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(
            viewState.items.allItems!!,
        ) { _, item ->
            Avatar(avatar = item, isSelected = viewState.selectedId == item.id, onAvatarSelect = onAvatarSelect)
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun Avatar(
    avatar: Avatar,
    isSelected: Boolean,
    onAvatarSelect: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable { onAvatarSelect(avatar.id, avatar.displayName) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .padding(bottom = 4.dp),
        ) {
            AsyncImage(
                model = avatar.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            if (isSelected) {
                SelectedBadge(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = avatar.displayName,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )

            Text(
                text = avatar.updatedAt.toFormattedDateString(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6F),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
