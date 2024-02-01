package de.westnordost.streetcomplete.view.image_select

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R

/** A dialog in which you can select one item of a range of items */
class ImageListPickerDialog<T>(
    context: Context,
    items: List<DisplayItem<T>>,
    @LayoutRes cellLayoutId: Int = R.layout.cell_labeled_image_select,
    columns: Int = 2,
    @StringRes titleResId: Int = R.string.quest_select_hint,
    onSelection: (DisplayItem<T>) -> Unit
) : AlertDialog(context) {

    init {
        val horizontalMargin = context.resources.getDimensionPixelOffset(R.dimen.dialog_horizontal_margin)
        val verticalMargin = context.resources.getDimensionPixelOffset(R.dimen.dialog_vertical_margin)

        val recyclerView = RecyclerView(context)
        recyclerView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        recyclerView.layoutManager = GridLayoutManager(context, columns)
        recyclerView.setPadding(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin)

        setTitle(titleResId)
        setView(recyclerView)

        val adapter = ImageSelectAdapter<T>(1)
        adapter.cellLayoutId = cellLayoutId
        adapter.items = items
        adapter.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                dismiss()
                onSelection(adapter.items[index])
            }

            override fun onIndexDeselected(index: Int) {}
        })
        recyclerView.adapter = adapter
    }
}
