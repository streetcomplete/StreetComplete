package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.databinding.FragmentOverlayRoadSurfaceSelectBinding
import de.westnordost.streetcomplete.osm.surface.SingleSurface
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceInfo
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAnswer
import de.westnordost.streetcomplete.osm.surface.SurfaceMissing
import de.westnordost.streetcomplete.osm.surface.SurfaceMissingWithNote
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.createMainSurfaceStatus
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.quests.surface.GENERIC_ROAD_SURFACES
import de.westnordost.streetcomplete.quests.surface.shouldBeDescribed
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class RoadSurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_road_surface_select
    private val binding by contentViewBinding(FragmentOverlayRoadSurfaceSelectBinding::bind)

    private val itemsPerRow = 2
    /** items to display. May not be accessed before onCreate */
    val items: List<DisplayItem<Surface>> = Surface.values().filter { it !in GENERIC_ROAD_SURFACES }.map { it.asItem() }
    private val cellLayoutId: Int = R.layout.cell_icon_select_with_label_below
    private var currentStatus: SingleSurfaceInfo? = null

    private var selectedStatusForMainSurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

    sealed class SingleSurfaceItemInfo
    data class SingleSurfaceItem(val surface: DisplayItem<Surface>) : SingleSurfaceItemInfo()
    data class SingleSurfaceItemWithNote(val surface: DisplayItem<Surface>, val note: String) : SingleSurfaceItemInfo()

    fun collectSurfaceData(callback: (SingleSurfaceItemInfo) -> Unit) {
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

        binding.selectButtonMainSurface.setOnClickListener {
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

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectedCellViewMainSurface, true)
        binding.selectedCellViewMainSurface.children.first().background = null

        // TODO remove in PR
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

        val status = createMainSurfaceStatus(element!!.tags)
        currentStatus = status
        when (status) {
            is SingleSurface -> {
                selectedStatusForMainSurface = status.surface.asItem()
            }
            is SingleSurfaceWithNote -> {
                binding.explanationInputMainSurface.text = SpannableStringBuilder(status.note)
                selectedStatusForMainSurface = status.surface.asItem()
            }
            is SurfaceMissingWithNote -> {
                binding.explanationInputMainSurface.text = SpannableStringBuilder(status.note)
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
            binding.explanationInputMainSurfaceContainer.isVisible = mainSurfaceItem.value?.shouldBeDescribed ?: false
        } else if (noteText() != null) {
            binding.explanationInputMainSurfaceContainer.isVisible = true
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
        if (selectedStatusForMainSurface == null) {
            return false
        }
        val surfaceValue = selectedStatusForMainSurface!!.value
        val note = noteText()
        if (surfaceValue == null) {
            return false
        }
        if (surfaceValue.shouldBeDescribed) {
            return note != null
        }
        return true
    }

    fun noteText(): String? {
        return binding.explanationInputMainSurface.nonBlankTextOrNull
    }

    override fun hasChanges(): Boolean {
        return when (val status = currentStatus) {
            is SingleSurface -> selectedStatusForMainSurface?.value != status.surface
            is SingleSurfaceWithNote -> selectedStatusForMainSurface?.value != status.surface || noteText() != status.note
            is SurfaceMissing -> selectedStatusForMainSurface?.value != null
            is SurfaceMissingWithNote -> selectedStatusForMainSurface?.value != null || noteText() != status.note
            null -> throw Exception("it was supposed to be set in onViewCreated - is it possible to trigger it before onViewCreated completes?")
        }
    }

    override fun onClickOk() {
        val note = noteText()
        val surfaceObject = selectedStatusForMainSurface!!.value!!
        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element!!.tags).also {
            editTags(it, element!!.tags, surfaceObject, note)
        }.create()))
    }

    companion object {
        fun editTags(changesBuilder: StringMapChangesBuilder, presentTags: Map<String, String>, surfaceObject: Surface, note: String?) {
            SurfaceAnswer(surfaceObject, note).applyTo(changesBuilder)
        }

        private const val SELECTED_MAIN_SURFACE_INDEX = "selected_main_surface_index"
        private const val SELECTED_MAIN_SURFACE_NOTE_TEXT = "selected_main_surface_note_text"
    }
}
