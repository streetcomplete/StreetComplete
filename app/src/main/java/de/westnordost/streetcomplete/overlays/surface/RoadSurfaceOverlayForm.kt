// TODO switching between surface and cycleway:surface / footway:surface modes
package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.databinding.FragmentOverlayRoadSurfaceSelectBinding
import de.westnordost.streetcomplete.osm.CyclewayFootwaySurfaces
import de.westnordost.streetcomplete.osm.SingleSurface
import de.westnordost.streetcomplete.osm.SingleSurfaceInfo
import de.westnordost.streetcomplete.osm.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.osm.SurfaceInfo
import de.westnordost.streetcomplete.osm.SurfaceMissing
import de.westnordost.streetcomplete.osm.commonSurfaceDescription
import de.westnordost.streetcomplete.osm.createMainSurfaceStatus
import de.westnordost.streetcomplete.osm.createSurfaceStatus
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.surface.GENERIC_ROAD_SURFACES
import de.westnordost.streetcomplete.quests.surface.asItem
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class RoadSurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_road_surface_select
    private val binding by contentViewBinding(FragmentOverlayRoadSurfaceSelectBinding::bind)

    private val itemsPerRow = 3 // TODO: maybe 2 for normal user?
    /** items to display. May not be accessed before onCreate */
    val items: List<DisplayItem<Surface>> = Surface.values().filter { it !in GENERIC_ROAD_SURFACES }.map { it.asItem() }
    private val cellLayoutId: Int = R.layout.cell_icon_select_with_label_below
    private var currentStatus: SingleSurfaceInfo? = null

    private var selectedStatusForMainSurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectButtonMainSurface.setOnClickListener {
            ImageListPickerDialog(requireContext(), items, cellLayoutId, itemsPerRow) { item ->
                if (item != selectedStatusForMainSurface) {
                    selectedStatusForMainSurface = item
                    checkIsFormComplete()
                }
            }.show()
        }

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectedCellViewMainSurface, true)
        binding.selectedCellViewMainSurface.children.first().background = null

        // if answer not selected already: open it immediately - user will confirm it later
        // so it should fine even in case of a missclick
        /*
        if (selectedStatusForMainSurface?.value == null) {
            ImageListPickerDialog(requireContext(), items, cellLayoutId, itemsPerRow) { item ->
                if (item != selectedStatusForMainSurface) {
                    selectedStatusForMainSurface = item
                    checkIsFormComplete()
                }
            }.show()
        }
         */

        val status = createMainSurfaceStatus(element.tags)
        currentStatus = status
        when (status) {
            is SingleSurface -> {
                selectedStatusForMainSurface = status.surface.asItem()
            }
            is SingleSurfaceWithNote -> {
                binding.explanationInput.text = SpannableStringBuilder(status.note)
                selectedStatusForMainSurface = status.surface.asItem()
            }
            is SurfaceMissing -> {
                // nothing to do
            }
        }
        updateSelectedCell()
    }

    private fun updateSelectedCell() {
        val mainSurfaceItem = selectedStatusForMainSurface
        binding.selectTextViewMainSurface.isGone = mainSurfaceItem != null
        binding.selectedCellViewMainSurface.isGone = mainSurfaceItem == null
        if (mainSurfaceItem != null) {
            ItemViewHolder(binding.selectedCellViewMainSurface).bind(mainSurfaceItem)
        }
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        val selectedMainSurfaceIndex = inState.getInt(SELECTED_MAIN_SURFACE_INDEX)
        selectedStatusForMainSurface = if (selectedMainSurfaceIndex != -1) items[selectedMainSurfaceIndex] else null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_MAIN_SURFACE_INDEX, items.indexOf(selectedStatusForMainSurface))
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete() = selectedStatusForMainSurface != null

    companion object {
        private const val SELECTED_MAIN_SURFACE_INDEX = "selected_main_surface_index"
    }

    fun noteText(): String {
        return binding.explanationInput.text.toString().trim()
    }

    override fun hasChanges(): Boolean {
        return when (val status = currentStatus) {
            is SingleSurface -> selectedStatusForMainSurface?.value != status.surface
            is SingleSurfaceWithNote -> selectedStatusForMainSurface?.value != status.surface || noteText() != status.note
            is SurfaceMissing -> selectedStatusForMainSurface?.value != null
            null -> throw Exception("null pointer exceeeeeeeeeeption (should be impossible)")
        }
    }

    override fun onClickOk() {
        val note = noteText()
        if (note == "") {
            // TODO handle it robustly and move to path/universal overlays
            // TODO do not allow to remove surface:note if surface=paved/unpaved is set
            if (currentStatus is SingleSurfaceWithNote) {
                applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
                    it.remove("surface:note")
                }.create()))
            }
        } else {
            applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
                it["surface:note"] = note
            }.create()))
        }

        if (selectedStatusForMainSurface != null) {
            applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
                it.updateWithCheckDate("surface", selectedStatusForMainSurface!!.value!!.osmValue)
            }.create()))
        }
    }
}
