package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.lazy.LazyListState

val LazyListState.isScrolledToEnd: Boolean get() {
    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastItem == null || lastItem.offset + lastItem.size <= layoutInfo.viewportEndOffset
}

fun LazyListState.isItemAtIndexFullyVisible(index: Int): Boolean {
    val item = layoutInfo.visibleItemsInfo.find { it.index == index }
    return item != null &&
        item.offset >= 0 &&
        item.offset + item.size <= layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
}
