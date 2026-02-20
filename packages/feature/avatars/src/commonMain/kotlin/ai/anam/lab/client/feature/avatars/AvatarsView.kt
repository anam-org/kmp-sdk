package ai.anam.lab.client.feature.avatars

import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.isOneShot
import ai.anam.lab.client.core.datetime.toFormattedDateString
import ai.anam.lab.client.core.ui.components.CollapsibleHeader
import ai.anam.lab.client.core.ui.components.PaginationEmptySearchIndicator
import ai.anam.lab.client.core.ui.components.PaginationErrorIndicator
import ai.anam.lab.client.core.ui.components.PaginationProgressIndicator
import ai.anam.lab.client.core.ui.components.SearchBar
import ai.anam.lab.client.core.ui.components.SelectedBadge
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.avatars_delete_content_description
import ai.anam.lab.client.core.ui.resources.generated.resources.avatars_one_shot
import ai.anam.lab.client.core.ui.resources.generated.resources.create_avatar_one_shot_avatar
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.ahmad_hamwi.compose.pagination.PaginatedLazyVerticalGrid
import kotlin.time.ExperimentalTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun AvatarsView(modifier: Modifier = Modifier, viewModel: AvatarsViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    AvatarsView(
        viewState = viewState,
        onAvatarSelect = viewModel::setAvatar,
        onDeleteAvatar = viewModel::deleteAvatar,
        onQueryChange = viewModel::onQueryChange,
        onOneShotChange = viewModel::onOneShotChange,
        onResetFilters = viewModel::resetFilters,
        onCreateAvatarClick = viewModel::navigateToCreateAvatar,
        modifier = modifier,
    )
}

@Composable
fun AvatarsView(
    viewState: AvatarsViewState,
    onAvatarSelect: (String, String) -> Unit,
    onDeleteAvatar: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onOneShotChange: (Boolean) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
    onCreateAvatarClick: () -> Unit = {},
    minColumns: Int = 2,
    maxColumns: Int = 4,
    minColumnWidth: Dp = 160.dp,
) {
    BoxWithConstraints(modifier = modifier) {
        val columnCount = (maxWidth / minColumnWidth).toInt().coerceIn(minColumns, maxColumns)

        CollapsibleHeader(
            header = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    SearchBar(
                        query = viewState.query,
                        onQueryChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Checkbox(
                                checked = viewState.onlyOneShot,
                                onCheckedChange = onOneShotChange,
                            )
                            Text(
                                text = stringResource(Res.string.avatars_one_shot),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            PaginatedLazyVerticalGrid(
                paginationState = viewState.items,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                columns = GridCells.Fixed(columnCount),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                firstPageProgressIndicator = { PaginationProgressIndicator() },
                newPageProgressIndicator = {
                    PaginationProgressIndicator(
                        modifier = Modifier.aspectRatio(4f / 3f),
                    )
                },
                firstPageErrorIndicator = { exception ->
                    PaginationErrorIndicator(
                        exception = exception,
                        onRetry = { viewState.items.retryLastFailedRequest() },
                    )
                },
                firstPageEmptyIndicator = {
                    if (viewState.query.isNotBlank() || viewState.onlyOneShot) {
                        PaginationEmptySearchIndicator(
                            onReset = onResetFilters,
                        )
                    }
                },
            ) {
                item {
                    CreateAvatarItem(onClick = onCreateAvatarClick)
                }
                itemsIndexed(
                    viewState.items.allItems!!,
                ) { _, item ->
                    Avatar(
                        avatar = item,
                        isSelected = viewState.selectedId == item.id,
                        onAvatarSelect = onAvatarSelect,
                        onDeleteAvatar = if (item.isOneShot()) onDeleteAvatar else null,
                    )
                }
            }
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
    onDeleteAvatar: ((String) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .clickable { onAvatarSelect(avatar.id, avatar.displayName) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .padding(bottom = 4.dp),
        ) {
            AsyncImage(
                model = avatar.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    SelectedBadge(modifier = Modifier.size(24.dp))
                }
            }

            if (onDeleteAvatar != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.BottomEnd)
                        .clickable { onDeleteAvatar(avatar.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.avatars_delete_content_description),
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                }
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

@Composable
private fun CreateAvatarItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor = MaterialTheme.colorScheme.outline
    Column(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .padding(bottom = 4.dp)
                .border(
                    border = BorderStroke(2.dp, borderColor),
                    shape = RoundedCornerShape(8.dp),
                )
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = stringResource(Res.string.create_avatar_one_shot_avatar),
                modifier = Modifier.size(40.dp),
                tint = borderColor,
            )
        }

        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = stringResource(Res.string.create_avatar_one_shot_avatar),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
