package de.westnordost.streetcomplete.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/** Item decoration that adds a spacing between the items for RecyclerView that uses a GridLayoutManager*/
class GridLayoutSpacingItemDecoration(private val spacingInPx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val count = parent.adapter?.itemCount ?: 0
        val layoutManager = parent.layoutManager as GridLayoutManager

        val spanCount = layoutManager.spanCount
        val spanSizeLookup = layoutManager.spanSizeLookup

        val row = spanSizeLookup.getSpanGroupIndex(position, spanCount)
        val span = spanSizeLookup.getSpanIndex(position, spanCount)
        val rowCount = spanSizeLookup.getSpanGroupIndex(count - 1, spanCount) + 1

        outRect.left = if (span > 0) spacingInPx/2 else 0
        outRect.right = if (span < spanCount - 1) spacingInPx/2 else 0
        outRect.top = if (row > 0) spacingInPx/2 else 0
        outRect.bottom = if (row < rowCount - 1) spacingInPx/2 else 0
    }
}
