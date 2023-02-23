package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.databinding.FragmentOverlayRoadSurfaceSelectBinding
import de.westnordost.streetcomplete.osm.surface.COMMON_SPECIFIC_PAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.COMMON_SPECIFIC_UNPAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.GENERIC_ROAD_SURFACES
import de.westnordost.streetcomplete.osm.surface.GROUND_SURFACES
import de.westnordost.streetcomplete.osm.surface.SurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.createMainSurfaceStatus
import de.westnordost.streetcomplete.osm.surface.shouldBeDescribed
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class RoadSurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_road_surface_select
    private val containerBinding by contentViewBinding(FragmentOverlayRoadSurfaceSelectBinding::bind)
    private val binding get() = containerBinding.form

    private val noteText get() = binding.explanationInput.nonBlankTextOrNull

    /** items to display. May not be accessed before onCreate */
    val items: List<DisplayItem<Surface>> = (COMMON_SPECIFIC_PAVED_SURFACES + COMMON_SPECIFIC_UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_ROAD_SURFACES).toItems()
    private val cellLayoutId: Int = R.layout.cell_labeled_icon_select

    private var originalSurfaceStatus: SurfaceWithNote? = null

    private var selectedSurfaceItem: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.explanationInput.doAfterTextChanged { checkIsFormComplete() }

        binding.selectButton.root.setOnClickListener {
            collectSurfaceData { surface: DisplayItem<Surface>, note: String? ->
                selectedSurfaceItem = surface
                binding.explanationInput.setText(note)
                checkIsFormComplete()
            }
        }

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectButton.selectedCellView, true)
        binding.selectButton.selectedCellView.children.first().background = null

        val status = createMainSurfaceStatus(element!!.tags)
        originalSurfaceStatus = status
        selectedSurfaceItem = status.value?.asItem()
        binding.explanationInput.setText(status.note)
        updateSelectedCell()
    }

    private fun collectSurfaceData(callback: (surface: DisplayItem<Surface>, note: String?) -> Unit) {
        ImageListPickerDialog(requireContext(), items, cellLayoutId, 2) { item ->
            val value = item.value
            if (value != null && value.shouldBeDescribed) {
                DescribeGenericSurfaceDialog(requireContext()) { description ->
                    callback(item, description)
                }.show()
            } else {
                callback(item, null)
            }
        }.show()
    }

    private fun updateSelectedCell() {
        val surfaceItem = selectedSurfaceItem
        binding.selectButton.selectTextView.isGone = surfaceItem != null
        binding.selectButton.selectedCellView.isGone = surfaceItem == null
        if (surfaceItem != null) {
            ItemViewHolder(binding.selectButton.selectedCellView).bind(surfaceItem)
        }
        if (noteText != null || surfaceItem?.value?.shouldBeDescribed == true) {
            binding.explanationInput.isVisible = true
        }
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        val selectedIndex = inState.getInt(SELECTED_INDEX)
        selectedSurfaceItem = if (selectedIndex != -1) items[selectedIndex] else null
        binding.explanationInput.setText(inState.getString(NOTE_TEXT))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_INDEX, items.indexOf(selectedSurfaceItem))
        outState.putString(NOTE_TEXT, noteText)
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete(): Boolean {
        val surfaceValue = selectedSurfaceItem?.value ?: return false
        if (surfaceValue.shouldBeDescribed) {
            return noteText != null
        }
        return true
    }

    override fun hasChanges(): Boolean =
        selectedSurfaceItem?.value != originalSurfaceStatus?.value
        || noteText != originalSurfaceStatus?.note

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        SurfaceAndNote(selectedSurfaceItem!!.value!!, noteText).applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(tagChanges.create()))
    }

    companion object {
        private const val SELECTED_INDEX = "selected_index"
        private const val NOTE_TEXT = "note_text"
    }
}
