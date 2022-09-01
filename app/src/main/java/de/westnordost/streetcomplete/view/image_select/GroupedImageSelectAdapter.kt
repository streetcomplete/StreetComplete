package de.westnordost.streetcomplete.view.image_select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R

/** Select one items from a groupable list of items  */
class GroupedImageSelectAdapter<T>(val gridLayoutManager: GridLayoutManager) :
    RecyclerView.Adapter<ItemViewHolder>() {

    var cellLayoutId = R.layout.cell_labeled_image_select
    var groupCellLayoutId = R.layout.cell_panorama_select

    private var _items = mutableListOf<GroupableDisplayItem<T>>()
    var items: List<GroupableDisplayItem<T>>
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

    var selectedItem: GroupableDisplayItem<T>? = null
        private set

    // group is also remembered as item may be appearing
    // once outside any groups and again in the group
    private var selectedItemGroup: GroupableDisplayItem<T>? = null

    private val selectedIndex: Int get() =
        selectedItem?.let { indexOfItemGivenGroupMembership(it, selectedItemGroup) } ?: -1

    private fun indexOfItemGivenGroupMembership(item: GroupableDisplayItem<T>, group: GroupableDisplayItem<T>?): Int {
        var currentGroup: GroupableDisplayItem<T> ? = null
        for (i in 0 until _items.size) {
            if (_items[i].isGroup) {
                currentGroup = _items[i]
            }
            if (item == _items[i] && group == currentGroup) {
                return i
            }
        }
        return -1
    }

    val listeners = mutableListOf<(GroupableDisplayItem<T>?) -> Unit>()

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
        val selectedIndex = selectedIndex
        holder.isSelected = selectedIndex == position
        holder.isGroupExpanded = getGroupIndex(selectedIndex) == position
    }

    private fun toggle(index: Int) {
        val previousItem = selectedItem
        val previousGroup = selectedItemGroup

        val foundGroupIndex = getGroupIndex(index)
        selectedItemGroup = if (foundGroupIndex == -1) null else _items[foundGroupIndex]
        selectedItem = _items[index]

        if (previousItem != null) {
            val previousItemIndex = indexOfItemGivenGroupMembership(previousItem, previousGroup)
            val previousGroupIndex = getGroupIndex(previousItemIndex)
            notifyItemChanged(previousItemIndex)
            if (previousGroup != null) {
                if (selectedItem == null || previousGroup != selectedItemGroup) {
                    retractGroup(previousGroupIndex)
                }
            }
        }
        val selectedItem = selectedItem
        if (selectedItem != null) {
            val selectedIndex = selectedIndex
            notifyItemChanged(selectedIndex)

            if (selectedItem.isGroup) {
                if (previousItem == null || previousGroup != selectedItemGroup) {
                    expandGroup(selectedIndex)
                }
            }
        }
        for (listener in listeners) {
            listener(selectedItem)
        }
    }

    private fun getGroupIndex(index: Int): Int {
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
        notifyItemChanged(index)
        notifyItemRangeInserted(index + 1, subItems.size)
    }

    private fun retractGroup(index: Int) {
        val item = _items[index]
        val subItems = item.items!!
        for (i in subItems.indices) {
            _items.removeAt(index + 1)
        }
        notifyItemChanged(index)
        notifyItemRangeRemoved(index + 1, subItems.size)
    }

    override fun getItemCount() = _items.size

    override fun getItemViewType(position: Int) = if (_items[position].isGroup) GROUP else CELL

    companion object {
        private const val GROUP = 0
        private const val CELL = 1
    }
}
