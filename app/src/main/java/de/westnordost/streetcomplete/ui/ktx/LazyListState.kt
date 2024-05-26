package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.lazy.LazyListState

val LazyListState.isScrolledToEnd: Boolean get() {
    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastItem == null || lastItem.offset + lastItem.size <= layoutInfo.viewportEndOffset
}
