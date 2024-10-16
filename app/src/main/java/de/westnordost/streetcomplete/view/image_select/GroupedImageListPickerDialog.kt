package de.westnordost.streetcomplete.view.image_select

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R

/** A dialog in which you can select one grouped item of a range of items */
class GroupedImageListPickerDialog<T>(
    context: Context,
    items: List<GroupableDisplayItem<T>>,
    @LayoutRes groupCellLayoutId: Int,
    @LayoutRes cellLayoutId: Int,
    columns: Int = 3,
    @StringRes titleResId: Int = R.string.quest_select_hint_most_specific,
    onSelection: (GroupableDisplayItem<T>) -> Unit
) : AlertDialog(context) {

    private val adapter: GroupedImageSelectAdapter<T>

    init {
        val horizontalMargin = context.resources.getDimensionPixelOffset(R.dimen.dialog_horizontal_margin)
        val verticalMargin = context.resources.getDimensionPixelOffset(R.dimen.dialog_vertical_margin)

        val recyclerView = RecyclerView(context)
        recyclerView.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        adapter = GroupedImageSelectAdapter()
        adapter.groupCellLayoutId = groupCellLayoutId
        adapter.cellLayoutId = cellLayoutId
        adapter.items = items
        adapter.listeners.add { updateOkButtonEnablement() }

        val layoutManager = GridLayoutManager(context, columns)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (adapter.items[position].isGroup) layoutManager.spanCount else 1
        }
        recyclerView.layoutManager = GridLayoutManager(context, columns)
        recyclerView.setPadding(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin)

        setTitle(titleResId)
        setView(recyclerView)

        recyclerView.adapter = adapter

        setButton(BUTTON_POSITIVE, context.getText(android.R.string.ok)) { _, _ ->
            dismiss()
            adapter.selectedItem?.let(onSelection)
        }
        setButton(BUTTON_NEGATIVE, context.getText(android.R.string.cancel), null, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateOkButtonEnablement()
    }

    private fun updateOkButtonEnablement() {
        getButton(BUTTON_POSITIVE)?.isEnabled = adapter.selectedItem?.value != null
    }
}
