package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.text.SpannableStringBuilder
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
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNoteMayBeEmpty
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
    private val binding by contentViewBinding(FragmentOverlayRoadSurfaceSelectBinding::bind)

    private val itemsPerRow = 2
    /** items to display. May not be accessed before onCreate */
    val items: List<DisplayItem<Surface>> = (COMMON_SPECIFIC_PAVED_SURFACES + COMMON_SPECIFIC_UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_ROAD_SURFACES).toItems()
    private val cellLayoutId: Int = R.layout.cell_labeled_icon_select
    private var originalSurfaceStatus: SurfaceAndNoteMayBeEmpty? = null

    private var selectedStatusForMainSurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    private sealed class SingleSurfaceItemInfo
    private data class SingleSurfaceItem(val surface: DisplayItem<Surface>) : SingleSurfaceItemInfo()
    private data class SingleSurfaceItemWithNote(val surface: DisplayItem<Surface>, val note: String) : SingleSurfaceItemInfo()

    private fun collectSurfaceData(callback: (SingleSurfaceItemInfo) -> Unit) {
        ImageListPickerDialog(requireContext(), items, cellLayoutId, itemsPerRow) { item ->
            val value = item.value
            if (value != null && value.shouldBeDescribed) {
                DescribeGenericSurfaceDialog(requireContext()) { description ->
                    callback(SingleSurfaceItemWithNote(item, description))
                }.show()
            } else {
                callback(SingleSurfaceItem(item))
            }
        }.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.explanationInputMainSurface.doAfterTextChanged { checkIsFormComplete() }

        binding.selectButton.root.setOnClickListener {
            collectSurfaceData { gathered: SingleSurfaceItemInfo ->
                when (gathered) {
                    is SingleSurfaceItem -> {
                        selectedStatusForMainSurface = gathered.surface
                        binding.explanationInputMainSurface.text = null
                    }
                    is SingleSurfaceItemWithNote -> {
                        selectedStatusForMainSurface = gathered.surface
                        binding.explanationInputMainSurface.text = SpannableStringBuilder(gathered.note)
                    }
                }
                checkIsFormComplete()
            }
        }

        if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectButton.selectedCellView, true)
        binding.selectButton.selectedCellView.children.first().background = null

        val status = createMainSurfaceStatus(element!!.tags)
        originalSurfaceStatus = status
        if(status.value != null) {
            selectedStatusForMainSurface = status.value.asItem()
        }
        if(status.note != null) {
            binding.explanationInputMainSurface.text = SpannableStringBuilder(status.note)
        }
        updateSelectedCell()
    }

    private fun updateSelectedCell() {
        val mainSurfaceItem = selectedStatusForMainSurface
        binding.selectButton.selectTextView.isGone = mainSurfaceItem != null
        binding.selectButton.selectedCellView.isGone = mainSurfaceItem == null
        if (mainSurfaceItem != null) {
            ItemViewHolder(binding.selectButton.selectedCellView).bind(mainSurfaceItem)
        }
        if (noteText() != null || mainSurfaceItem?.value?.shouldBeDescribed == true) {
            binding.explanationInputMainSurface.isVisible = true
        }
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        val selectedMainSurfaceIndex = inState.getInt(SELECTED_MAIN_SURFACE_INDEX)
        val selectedMainSurfaceNoteText = inState.getString(SELECTED_MAIN_SURFACE_NOTE_TEXT)
        selectedStatusForMainSurface = if (selectedMainSurfaceIndex != -1) items[selectedMainSurfaceIndex] else null
        if (selectedMainSurfaceNoteText != null) {
            binding.explanationInputMainSurface.text = SpannableStringBuilder(selectedMainSurfaceNoteText)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_MAIN_SURFACE_INDEX, items.indexOf(selectedStatusForMainSurface))
        outState.putString(SELECTED_MAIN_SURFACE_NOTE_TEXT, noteText())
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete(): Boolean {
        if(!hasChanges()) {
            return false
        }
        val surfaceValue = selectedStatusForMainSurface!!.value
        if (surfaceValue == null) {
            return false
        }
        if (surfaceValue.shouldBeDescribed) {
            return noteText() != null
        }
        return true
    }

    fun noteText(): String? {
        return binding.explanationInputMainSurface.nonBlankTextOrNull
    }

    override fun hasChanges(): Boolean {
        // originalSurfaceStatus was supposed to be set in onViewCreated - is it possible to trigger this before onViewCreated completes?
        return selectedStatusForMainSurface?.value != originalSurfaceStatus!!.value || noteText() != originalSurfaceStatus!!.note
    }

    override fun onClickOk() {
        val note = noteText()
        val surfaceObject = selectedStatusForMainSurface!!.value!!
        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element!!.tags).also {
            SurfaceAndNote(surfaceObject, note).applyTo(it)
        }.create()))
    }

    companion object {
        private const val SELECTED_MAIN_SURFACE_INDEX = "selected_main_surface_index"
        private const val SELECTED_MAIN_SURFACE_NOTE_TEXT = "selected_main_surface_note_text"
    }
}
