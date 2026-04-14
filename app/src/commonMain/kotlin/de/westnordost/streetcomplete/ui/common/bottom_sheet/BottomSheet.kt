package de.westnordost.streetcomplete.ui.common.bottom_sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetState.*
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
        ConsumeNestedScrollConnection(
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

private fun ConsumeNestedScrollConnection(
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
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        state.anchoredDrag {
            val scrollFlingScope = object : ScrollScope {
                override fun scrollBy(pixels: Float): Float {
                    dragTo(state.offset + pixels)
                    return pixels
                }
            }
            with(flingBehavior) { scrollFlingScope.performFling(consumed.toFloat()) }
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

@Preview
@Composable
private fun BottomSheetPreview() {
    BottomSheet {
        Column(Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Green)
        ) {
            Box(Modifier.fillMaxWidth().height(50.dp).background(Color.Blue))
            Text(
                text = LoremIpsum(1000).values.joinToString(" "),
                modifier = Modifier.verticalScroll(state = rememberScrollState())
            )
        }
    }
}
