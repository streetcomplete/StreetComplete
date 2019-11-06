package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.ImageSelectAdapter
import de.westnordost.streetcomplete.view.Item

/** A dialog in which you can one item of a range of items */
class ImageListPickerDialog<T>(
    context: Context,
    items: List<Item<T>>,
    cellLayoutId: Int = R.layout.cell_labeled_image_select,
    columns: Int = 2,
    onSelection: (Item<T>) -> Unit) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    init {
        val recyclerView = RecyclerView(context)
        recyclerView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        recyclerView.layoutManager = GridLayoutManager(context, columns)

        setTitle(R.string.quest_select_hint)
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
