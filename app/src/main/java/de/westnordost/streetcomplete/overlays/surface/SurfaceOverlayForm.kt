package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.databinding.FragmentOverlayImageSelectBinding
import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.osm.createSurfaceStatus
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.surface.GENERIC_ROAD_SURFACES
import de.westnordost.streetcomplete.quests.surface.asItem
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class SurfaceOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_image_select
    private val binding by contentViewBinding(FragmentOverlayImageSelectBinding::bind)

    private val itemsPerRow = 3 // TODO: maybe 2 for normal user?
    /** items to display. May not be accessed before onCreate */
    val items: List<DisplayItem<Surface>> = Surface.values().filter { it !in GENERIC_ROAD_SURFACES }.map { it.asItem() }
    private val cellLayoutId: Int = R.layout.cell_icon_select_with_label_below
    private var currentStatus: Surface? = null

    var selectedItem: DisplayItem<Surface>? = null
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

        val status = createSurfaceStatus(element.tags)
        currentStatus = status
        if (status != null) {
            selectedItem = status.asItem()
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

    override fun hasChanges(): Boolean =
        selectedItem?.value != currentStatus

    override fun onClickOk() {
        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
            it.updateWithCheckDate("surface", selectedItem!!.value!!.osmValue)
        }.create()))
    }
}
