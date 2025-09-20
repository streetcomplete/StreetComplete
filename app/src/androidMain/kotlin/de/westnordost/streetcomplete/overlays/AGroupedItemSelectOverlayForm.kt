package de.westnordost.streetcomplete.overlays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.CellLastPickedButtonBinding
import de.westnordost.streetcomplete.databinding.FragmentGroupedOverlayImageSelectBinding
import de.westnordost.streetcomplete.ui.common.item_select.Group

/** Abstract base class for any overlay form in which the user selects a grouped item */
abstract class AGroupedItemSelectOverlayForm<G: Group<I>, I> : AbstractOverlayForm() {
    // mostly copy-pasta from AItemSelectOverlayForm / AGroupedItemSelectQuestForm :-(

    final override val contentLayoutResId = R.layout.fragment_grouped_overlay_image_select
    private val binding by contentViewBinding(FragmentGroupedOverlayImageSelectBinding::bind)

    protected open val itemsPerRow = 1

    /** all items to display. May not be accessed before onCreate */
    protected abstract val allItems: List<G>
    /** items to display that are shown as last picked answers. May not be accessed before onCreate */
    protected open val lastPickedItems: List<I> = emptyList()

    private lateinit var itemsByString: Map<String, I>

    var selectedItem: I? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    @Composable protected abstract fun BoxScope.GroupContent(item: G)

    @Composable protected abstract fun BoxScope.ItemContent(item: I)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsByString = allItems.flatMap { it.children }.associateBy { it.toString() }
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
        outState.putString(SELECTED, selectedItem?.toString())
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete() = selectedItem != null

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
    private val items: List<I>,
    private val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<LastPickedAdapter<I>.ViewHolder>() {

    inner class ViewHolder(
        private val binding: CellLastPickedButtonBinding,
        private val onItemClicked: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClicked(bindingAdapterPosition) }
        }

        fun onBind(item: I) {
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
