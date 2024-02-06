package de.westnordost.streetcomplete.overlays

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentOverlayImageSelectBinding
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder
import de.westnordost.streetcomplete.view.setImage

/** Abstract base class for any overlay form in which the user selects an image item */
abstract class AImageSelectOverlayForm<I> : AbstractOverlayForm() {

    final override val contentLayoutResId = R.layout.fragment_overlay_image_select
    private val binding by contentViewBinding(FragmentOverlayImageSelectBinding::bind)

    protected open val itemsPerRow = 2
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<DisplayItem<I>>
    /** item to display as last picked answer. May not be accessed before onCreate */
    protected open val lastPickedItem: DisplayItem<I>? = null

    protected open val cellLayoutId: Int = R.layout.cell_icon_select_with_label_below

    var selectedItem: DisplayItem<I>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectButton.root.setOnClickListener {
            ImageListPickerDialog(requireContext(), items, cellLayoutId, itemsPerRow) { item ->
                if (item != selectedItem) {
                    selectedItem = item
                    checkIsFormComplete()
                }
            }.show()
        }

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectButton.selectedCellView, true)
        binding.selectButton.selectedCellView.children.first().background = null

        binding.lastPickedButton.isGone = lastPickedItem == null
        binding.lastPickedButton.setImage(lastPickedItem?.image)
        binding.lastPickedButton.setOnClickListener {
            selectedItem = lastPickedItem
            binding.lastPickedButton.isGone = true
            checkIsFormComplete()
        }

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
        val selectedIndex = inState.getInt(SELECTED_INDEX)
        selectedItem = if (selectedIndex != -1) items[selectedIndex] else null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_INDEX, items.indexOf(selectedItem))
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete() = selectedItem != null

    companion object {
        private const val SELECTED_INDEX = "selected_index"
    }
}
