package de.westnordost.streetcomplete.view.image_select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.Listeners

/** Select a number of items from a list of items  */
class ImageSelectAdapter<T>(private val maxSelectableIndices: Int = -1) :
    RecyclerView.Adapter<ItemViewHolder>() {

    var items = listOf<DisplayItem<T>>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val _selectedIndices = mutableSetOf<Int>()
    val selectedIndices get() = _selectedIndices.toList()

    var cellLayoutId = R.layout.cell_labeled_image_select

    val listeners = Listeners<OnItemSelectionListener>()

    val selectedItems get() = _selectedIndices.map { i -> items[i] }

    interface OnItemSelectionListener {
        fun onIndexSelected(index: Int)
        fun onIndexDeselected(index: Int)
    }

    fun indexOf(item: T): Int = items.indexOfFirst { it.value == item }

    fun select(indices: List<Int>) {
        for (index in indices) {
            select(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(cellLayoutId, parent, false)
        val holder = ItemViewHolder(view)
        holder.onClickListener = ::toggle
        return holder
    }

    fun isSelected(index: Int) = _selectedIndices.contains(index)

    fun select(index: Int) {
        checkIndexRange(index)
        // special case: toggle-behavior if only one index can be selected
        if (maxSelectableIndices == 1 && _selectedIndices.size == 1) {
            deselect(_selectedIndices.first())
        } else if (maxSelectableIndices > -1 && maxSelectableIndices <= _selectedIndices.size) {
            return
        }

        if (!_selectedIndices.add(index)) return

        notifyItemChanged(index)
        listeners.forEach { it.onIndexSelected(index) }
    }

    fun deselect(index: Int) {
        checkIndexRange(index)
        if (!_selectedIndices.remove(index)) return

        notifyItemChanged(index)
        listeners.forEach { it.onIndexDeselected(index) }
    }

    fun toggle(index: Int) {
        checkIndexRange(index)
        if (!isSelected(index)) {
            select(index)
        } else {
            deselect(index)
        }
    }

    private fun checkIndexRange(index: Int) {
        if (index < 0 || index >= items.size) {
            throw ArrayIndexOutOfBoundsException(index)
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
        holder.isSelected = isSelected(position)
    }

    override fun getItemCount() = items.size
}
