package de.westnordost.streetcomplete.view

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import de.westnordost.streetcomplete.R

/** Select one items from a groupable list of items  */
class GroupedImageSelectAdapter<T>(val gridLayoutManager: GridLayoutManager) :
    RecyclerView.Adapter<ItemViewHolder>() {

    var cellLayoutId = R.layout.cell_labeled_image_select
    var groupCellLayoutId = R.layout.cell_panorama_select

    private var _items = mutableListOf<Item<T>>()
    var items: List<Item<T>>
    set(value) {
        _items.clear()
        _items.addAll(value)
        selectedItem = null
        for (listener in listeners) {
            listener(null)
        }
        notifyDataSetChanged()
    }
    get() = _items.toList()

    var selectedItem: Item<T>? = null
        private set

    val listeners = mutableListOf<(Item<T>?) -> Unit>()

    init {
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (_items[position].isGroup) gridLayoutManager.spanCount else 1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutId = if (viewType == GROUP) groupCellLayoutId else cellLayoutId
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        val holder = ItemViewHolder(view)
        holder.onClickListener = ::toggle
        return holder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(_items[position])
        holder.isSelected = selectedItem?.let { _items.indexOf(it) == position } == true
    }

    private fun toggle(index: Int) {
        val prevSelectedItem = selectedItem
        if (selectedItem == null || prevSelectedItem !== _items[index]) {
            selectedItem = _items[index]
        } else {
            selectedItem = null
        }

        val selectedItem = selectedItem
        if (prevSelectedItem != null) {
            val prevSelectedIndex = _items.indexOf(prevSelectedItem)
            notifyItemChanged(prevSelectedIndex)

            val previousGroupIndex = getGroup(prevSelectedIndex)
            if (previousGroupIndex != -1) {
                if (selectedItem == null || previousGroupIndex != getGroup(_items.indexOf(selectedItem))) {
                    retractGroup(previousGroupIndex)
                }
            }
        }
        if (selectedItem != null) {
            val selectedIndex = _items.indexOf(selectedItem)
            notifyItemChanged(selectedIndex)

            if (selectedItem.isGroup) {
                if (prevSelectedItem == null || getGroup(_items.indexOf(prevSelectedItem)) != selectedIndex) {
                    expandGroup(selectedIndex)
                }
            }
        }
        for (listener in listeners) {
            listener(selectedItem)
        }
    }

    private fun getGroup(index: Int): Int {
        for (i in index downTo 0) {
            if (_items[i].isGroup) return i
        }
        return -1
    }

    private fun expandGroup(index: Int) {
        val item = _items[index]
        val subItems = item.items!!
        for (i in subItems.indices) {
            _items.add(index + i + 1, subItems[i])
        }
        notifyItemRangeInserted(index + 1, subItems.size)
    }

    private fun retractGroup(index: Int) {
        val item = _items[index]
        val subItems = item.items!!
        for (i in subItems.indices) {
            _items.removeAt(index + 1)
        }
        notifyItemRangeRemoved(index + 1, subItems.size)
    }

    override fun getItemCount() = _items.size

    override fun getItemViewType(position: Int) = if (_items[position].isGroup) GROUP else CELL

    companion object {
        private const val GROUP = 0
        private const val CELL = 1
    }
}
