package de.westnordost.streetcomplete.overlays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.CellLastPickedButtonBinding
import de.westnordost.streetcomplete.databinding.FragmentGroupedOverlayImageSelectBinding
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.GroupedImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder
import de.westnordost.streetcomplete.view.setImage

/** Abstract base class for any overlay form in which the user selects a grouped item */
abstract class AGroupedImageSelectOverlayForm<I> : AbstractOverlayForm() {
    // mostly copy-pasta from AImageSelectOverlayForm / AGroupedImageListQuestForm :-(

    final override val contentLayoutResId = R.layout.fragment_grouped_overlay_image_select
    private val binding by contentViewBinding(FragmentGroupedOverlayImageSelectBinding::bind)

    protected open val itemsPerRow = 1

    /** all items to display. May not be accessed before onCreate */
    protected abstract val allItems: List<GroupableDisplayItem<I>>
    /** items to display that are shown as last picked answers. May not be accessed before onCreate */
    protected open val lastPickedItems: List<GroupableDisplayItem<I>> = emptyList()

    protected open val cellLayoutId: Int = R.layout.cell_labeled_icon_select_with_description
    protected open val groupCellLayoutId: Int = R.layout.cell_labeled_icon_select_with_description_group

    private lateinit var itemsByString: Map<String, GroupableDisplayItem<I>>

    var selectedItem: GroupableDisplayItem<I>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = allItems.mapNotNull { it.items }.flatten().associateBy { it.value.toString() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectButton.root.setOnClickListener {
            GroupedImageListPickerDialog(requireContext(), allItems, groupCellLayoutId, cellLayoutId, itemsPerRow) { item ->
                if (item != selectedItem) {
                    selectedItem = item
                    checkIsFormComplete()
                }
            }.show()
        }

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectButton.selectedCellView, true)
        binding.selectButton.selectedCellView.children.first().background = null

        binding.lastPickedButtons.isGone = lastPickedItems.isEmpty()
        binding.lastPickedButtons.adapter = LastPickedAdapter(lastPickedItems, ::onLastPickedButtonClicked)

        updateSelectedCell()
    }

    private fun updateSelectedCell() {
        val item = selectedItem
        binding.selectButton.selectTextView.isGone = item != null
        binding.selectButton.selectedCellView.isGone = item == null
        if (item != null) {
            ItemViewHolder(binding.selectButton.selectedCellView).bind(item)
        }
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        selectedItem = inState.getString(SELECTED)?.let { itemsByString[it] }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SELECTED, selectedItem?.value?.toString())
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete() = selectedItem?.value != null

    /* --------------------------------------- fav items ---------------------------------------- */

    private fun onLastPickedButtonClicked(position: Int) {
        val item = lastPickedItems[position]
        selectedItem = item
        checkIsFormComplete()
    }

    companion object {
        private const val SELECTED = "selected"
    }
}

private class LastPickedAdapter<I>(
    private val items: List<GroupableDisplayItem<I>>,
    private val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<LastPickedAdapter<I>.ViewHolder>() {

    inner class ViewHolder(
        private val binding: CellLastPickedButtonBinding,
        private val onItemClicked: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClicked(bindingAdapterPosition) }
        }

        fun onBind(item: GroupableDisplayItem<I>) {
            binding.root.setImage(item.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellLastPickedButtonBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.onBind(items[position])
    }

    override fun getItemCount() = items.size
}
