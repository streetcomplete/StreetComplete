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

/** Abstract base class for any overlay form in which the user selects an image item */
abstract class AImageSelectOverlayForm<I> : AbstractOverlayForm() {

    final override val contentLayoutResId = R.layout.fragment_overlay_image_select
    private val binding by contentViewBinding(FragmentOverlayImageSelectBinding::bind)

    protected open val itemsPerRow = 2
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<DisplayItem<I>>
    protected open val cellLayoutId: Int = R.layout.cell_icon_select_with_label_below

    var selectedItem: DisplayItem<I>? = null
    set(value) {
        field = value
        updateSelectedCell()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectButton.setOnClickListener {
            ImageListPickerDialog(requireContext(), items, cellLayoutId, itemsPerRow) { item ->
                if (item != selectedItem) {
                    selectedItem = item
                    checkIsFormComplete()
                }
            }.show()
        }

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectedCellView, true)
        binding.selectedCellView.children.first().background = null

        updateSelectedCell()

        // if answer not selected already: open it immediately - user will confirm it later
        // so it should fine even in case of a missclick
        if (selectedItem == null) {
            ImageListPickerDialog(requireContext(), items, cellLayoutId, itemsPerRow) { item ->
                if (item != selectedItem) {
                    selectedItem = item
                    checkIsFormComplete()
                }
            }.show()
        }
    }

    private fun updateSelectedCell() {
        val item = selectedItem
        binding.selectTextView.isGone = item != null
        binding.selectedCellView.isGone = item == null
        if (item != null) {
            ItemViewHolder(binding.selectedCellView).bind(item)
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
