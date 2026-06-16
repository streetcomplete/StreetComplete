package de.westnordost.streetcomplete.ui.common.bottom_sheet

import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetState.Collapsed
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetState.Expanded
import de.westnordost.streetcomplete.ui.ktx.toPx
import kotlin.jvm.JvmName
import kotlin.math.max

enum class BottomSheetState { Collapsed, Expanded }

/** A simple bottom sheet, i.e. a Box that can be pulled up from below by dragging it up. Handles
 *  nested vertical scrolling properly. */
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    initialState: BottomSheetState = Collapsed,
    peekHeight: Dp = 64.dp,
    content: @Composable () -> Unit,
) {
    val state = rememberSaveable(saver = AnchoredDraggableState.Saver()) {
        AnchoredDraggableState(initialState)
    }
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(state)
    val nestedScrollConnection = remember(state, flingBehavior) {
        ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
            state = state,
            flingBehavior = flingBehavior,
            orientation = Orientation.Vertical
        )
    }
    val peekHeightPx = peekHeight.toPx()

    // outer box into which the sheet can slide into
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // inner box, i.e. the bottom sheet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    state.updateAnchors(DraggableAnchors {
                        Collapsed at max(size.height - peekHeightPx, 0f)
                        Expanded at 0f
                    })
                }
                // while offset hasn't been initialized yet, it should not be visible, to avoid
                // flickering
                .alpha(if (state.offset.isNaN()) 0f else 1f)
                .offset { IntOffset(0, state.offset.toInt()) }
                // necessary because drag events are usually dispatched to nested scroll views
                // first, we need to steal back control of it so we can first expand the sheet up
                // via drag before allowing to scroll in the nested view
                .nestedScroll(nestedScrollConnection)
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Vertical,
                    flingBehavior = flingBehavior
                ),
            content = { content() }
        )
    }
}

private fun ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
    state: AnchoredDraggableState<*>,
    flingBehavior: FlingBehavior,
    orientation: Orientation,
): NestedScrollConnection = object : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.toFloat()
        return if (delta < 0 && source == NestedScrollSource.UserInput) {
            state.dispatchRawDelta(delta).toOffset()
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        return if (source == NestedScrollSource.UserInput) {
            state.dispatchRawDelta(available.toFloat()).toOffset()
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = available.toFloat()
        val currentOffset = state.requireOffset()
        return if (toFling < 0 && currentOffset > state.anchors.minPosition()) {
            // since we go to the anchor with tween settling, consume all for the best UX
            state.anchoredDrag {
                val scrollFlingScope = object : ScrollScope {
                    override fun scrollBy(pixels: Float): Float {
                        dragTo(state.offset + pixels)
                        return pixels
                    }
                }
                with(flingBehavior) { scrollFlingScope.performFling(toFling) }
            }
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        // this API is crazy. Do I understand it? No. I got it from the example at
        // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/foundation/foundation/samples/src/main/java/androidx/compose/foundation/samples/AnchoredDraggableSample.kt;l=416-432;drc=7440f70755e3735dbd8f04718d12dfeec7584dc8
        // pointed at from the comment of the now deprecated `state.settle(velocity)` API.
        state.anchoredDrag {
            val scrollFlingScope = object : ScrollScope {
                override fun scrollBy(pixels: Float): Float {
                    dragTo(state.offset + pixels)
                    return pixels
                }
            }
            with(flingBehavior) { scrollFlingScope.performFling(available.toFloat()) }
        }
        return available
    }

    private fun Float.toOffset() = Offset(
        x = if (orientation == Orientation.Horizontal) this else 0f,
        y = if (orientation == Orientation.Vertical) this else 0f,
    )

    @JvmName("velocityToFloat")
    private fun Velocity.toFloat() = if (orientation == Orientation.Horizontal) x else y

    @JvmName("offsetToFloat")
    private fun Offset.toFloat(): Float = if (orientation == Orientation.Horizontal) x else y
}
