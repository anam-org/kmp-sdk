package ai.anam.lab.client.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A layout that places a collapsible [header] above scrollable [content]. The header starts fully collapsed
 * (zero height, hidden off-screen). When the user over-scrolls past the top of the [content] the header slides into
 * view; scrolling back down collapses it again. This is useful for search bars or filters that should be accessible
 * without permanently occupying screen space.
 *
 * After a fling gesture the header snaps to fully visible or fully hidden (whichever is closer) so it never rests in
 * a half-open state.
 *
 * The [content] lambda receives a [ColumnScope], so children can use [ColumnScope.weight] to fill remaining space.
 */
@Composable
fun CollapsibleHeader(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // headerHeightPx: the intrinsic (fully expanded) height of the header content, captured once on first measure.
    // headerOffsetPx: ranges from -headerHeightPx (fully hidden) to 0 (fully visible).
    val headerHeightPx = remember { mutableFloatStateOf(0f) }
    val headerOffsetPx = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    // Intercepts scroll gestures from the child (e.g. a LazyColumn/LazyVerticalGrid) to drive the header offset.
    val nestedScrollConnection = remember {
        CollapsibleHeaderNestedScrollConnection(headerHeightPx, headerOffsetPx, scope)
    }

    Column(modifier = modifier.nestedScroll(nestedScrollConnection)) {
        // Outer Box: clips the header content and uses a custom layout modifier to report only the
        // visible portion of the header as its height. This causes the Column to allocate exactly
        // the right amount of space, and clipToBounds hides the part that has slid off-screen.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val height = headerHeightPx.floatValue
                    val offset = headerOffsetPx.floatValue
                    // visibleHeight shrinks from full height (offset=0) to 0 (offset=-height).
                    val visibleHeight = (height + offset)
                        .coerceAtLeast(0f)
                        .roundToInt()
                    layout(placeable.width, visibleHeight) {
                        // Place at a negative Y to slide the header upward out of the clipping region.
                        placeable.placeRelative(0, offset.roundToInt())
                    }
                },
        ) {
            // Inner Box: captures the header's intrinsic height via onSizeChanged. On first measure
            // the offset is set to -height so the header starts fully hidden.
            Box(
                modifier = Modifier.onSizeChanged {
                    val height = it.height.toFloat()
                    if (headerHeightPx.floatValue == 0f) {
                        headerOffsetPx.floatValue = -height
                    }
                    headerHeightPx.floatValue = height
                },
            ) {
                header()
            }
        }

        content()
    }
}

/**
 * [NestedScrollConnection] that drives the collapsible header offset. Collapses on downward scroll via [onPreScroll]
 * (before the child list scrolls) and reveals on upward over-scroll via [onPostScroll] (after the child list can no
 * longer scroll). After a fling, [onPostFling] snaps the header to fully open or fully closed.
 */
private class CollapsibleHeaderNestedScrollConnection(
    private val headerHeightPx: MutableFloatState,
    private val headerOffsetPx: MutableFloatState,
    private val scope: CoroutineScope,
) : NestedScrollConnection {

    // Tracks the in-flight snap animation so it can be cancelled when a new scroll gesture starts.
    private var snapJob: Job? = null

    // onPreScroll fires BEFORE the child consumes the scroll. We use it to collapse the header on
    // downward scroll (negative delta) so it hides before the list starts scrolling.
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        snapJob?.cancel()
        val delta = available.y
        if (delta >= 0f) return Offset.Zero
        val height = headerHeightPx.floatValue
        if (height <= 0f) return Offset.Zero
        val oldOffset = headerOffsetPx.floatValue
        val newOffset = (oldOffset + delta).coerceIn(-height, 0f)
        headerOffsetPx.floatValue = newOffset
        return Offset(0f, newOffset - oldOffset)
    }

    // onPostScroll fires AFTER the child has consumed what it can. Any leftover upward scroll
    // (positive delta) means the list is already at the top — use it to reveal the header.
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        snapJob?.cancel()
        val delta = available.y
        if (delta <= 0f) return Offset.Zero
        val height = headerHeightPx.floatValue
        if (height <= 0f) return Offset.Zero
        val oldOffset = headerOffsetPx.floatValue
        val newOffset = (oldOffset + delta).coerceIn(-height, 0f)
        headerOffsetPx.floatValue = newOffset
        return Offset(0f, newOffset - oldOffset)
    }

    // After a fling ends, snap the header to fully visible or fully hidden (whichever is closer)
    // so it never rests in a half-open state.
    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val height = headerHeightPx.floatValue
        if (height <= 0f) return super.onPostFling(consumed, available)

        val currentOffset = headerOffsetPx.floatValue
        // Already fully snapped — nothing to do.
        if (currentOffset == 0f || currentOffset == -height) return super.onPostFling(consumed, available)

        // Snap to whichever edge is closer: 0 (visible) or -height (hidden).
        val target = if (currentOffset > -height / 2f) 0f else -height
        val animatable = Animatable(currentOffset)
        snapJob = scope.launch {
            animatable.animateTo(target) {
                headerOffsetPx.floatValue = value
            }
        }

        return super.onPostFling(consumed, available)
    }
}
