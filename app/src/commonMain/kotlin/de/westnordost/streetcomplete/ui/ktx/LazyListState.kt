package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.lazy.LazyListState

fun LazyListState.isItemAtIndexFullyVisible(index: Int): Boolean {
    val item = layoutInfo.visibleItemsInfo.find { it.index == index }
    return item != null &&
        item.offset >= 0 &&
        item.offset + item.size <= layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
}
