package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import kotlinx.coroutines.launch

@Composable
fun rememberWheelPickerState(selectedItemIndex: Int = 0) =
    remember { WheelPickerState(selectedItemIndex) }

class WheelPickerState(selectedItemIndex: Int = 0) : ScrollableState {

    internal val lazyListState = LazyListState(firstVisibleItemIndex = selectedItemIndex)

    val selectedItemIndex: Int by derivedStateOf {
        selectedItemInfo?.index ?: selectedItemIndex
    }

    internal val selectedItemInfo: LazyListItemInfo? by derivedStateOf {
        lazyListState.layoutInfo.findCenterItem()
    }

    override val canScrollBackward: Boolean
        get() = lazyListState.canScrollBackward

    override val canScrollForward: Boolean
        get() = lazyListState.canScrollForward

    override val isScrollInProgress: Boolean
        get() = lazyListState.isScrollInProgress

    suspend fun scrollToItem(index: Int) {
        lazyListState.scrollToItem(index)
    }

    suspend fun animateScrollToItem(index: Int) {
        lazyListState.animateScrollToItem(index)
    }

    override fun dispatchRawDelta(delta: Float): Float =
        lazyListState.dispatchRawDelta(delta)

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) {
        lazyListState.scroll(scrollPriority, block)
    }
}

/** A WheelPicker aka NumberPicker (in Android). Presents the selectable [items] on a draggable
 *  vertical wheel, the item displayed in the center is selected.
 *  [visibleAdjacentItems] determines how many adjacent items to the one that is selected should
 *  be displayed. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: WheelPickerState = rememberWheelPickerState(),
    key: ((T) -> Any)? = null,
    visibleAdjacentItems: Int = 1,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable (item: T) -> Unit
) {
    val scope = rememberCoroutineScope()

    val selectedItemHeight = (state.selectedItemInfo?.size ?: 0).pxToDp()

    val paddingValues = remember(visibleAdjacentItems, selectedItemHeight) {
        PaddingValues(vertical = selectedItemHeight * visibleAdjacentItems)
    }

    val visibleItemsCount = visibleAdjacentItems * 2 + 1

    LazyColumn(
        modifier = modifier
            .height(selectedItemHeight * visibleItemsCount)
            .fadingEdges(selectedItemHeight)
            .pickerIndicator(selectedItemHeight),
        state = state.lazyListState,
        contentPadding = paddingValues,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = rememberSnapFlingBehavior(state.lazyListState),
    ) {
        items(
            count = items.size,
            key = key?.let { { key(items[it]) } }
        ) { index ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(if (index == state.selectedItemIndex) 1f else 0.85f)
                    .pointerInput(index) {
                        detectTapGestures {
                            scope.launch { state.animateScrollToItem(index) }
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                content(items[index])
            }
        }
    }
}

/** the selected item is at full opacity, the items then fade off towards the edges */
@Composable
private fun Modifier.fadingEdges(selectedItemHeight: Dp): Modifier {
    val topGradient = remember {
        Brush.verticalGradient(
            0f to Color.Transparent,
            1f to Color.Black
        )
    }
    val bottomGradient = remember {
        Brush.verticalGradient(
            0f to Color.Black,
            1f to Color.Transparent
        )
    }

    return this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()

            val height = (size.height - selectedItemHeight.toPx()) / 2

            drawRect(
                topLeft = Offset.Zero,
                size = size.copy(height = height),
                brush = topGradient,
                blendMode = BlendMode.DstIn
            )
            drawRect(
                topLeft = Offset(0f, size.height - height),
                size = size.copy(height = height),
                brush = bottomGradient,
                blendMode = BlendMode.DstIn
            )
        }
}

/** frame drawn around selected value */
@Composable
private fun Modifier.pickerIndicator(selectedItemHeight: Dp): Modifier {
    val density = LocalDensity.current.density
    val color = MaterialTheme.colors.onSurface

    return drawWithContent {
        drawContent()

        val strokeWidth = 2f  * density
        val inset = (size.height - selectedItemHeight.toPx()) / 2

        inset(vertical = inset.coerceAtMost(size.height / 2)) {
            drawLine(
                color = color,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = strokeWidth,
            )
            drawLine(
                color = color,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = strokeWidth,
            )
        }
    }
}

private fun LazyListLayoutInfo.findCenterItem(): LazyListItemInfo? =
    visibleItemsInfo.fastFirstOrNull {
        it.offset + it.size - viewportStartOffset > viewportSize.height / 2
    }

@Preview
@Composable
private fun PreviewPicker() {
    WheelPicker(
        items = (1..100).toList(),
    ) { item ->
        Text(item.toString())
    }
}
