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
import de.westnordost.streetcomplete.databinding.FragmentOverlayPathSurfaceSelectBinding
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfaces
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfacesWithNote
import de.westnordost.streetcomplete.osm.surface.SingleSurface
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceInfo
import de.westnordost.streetcomplete.osm.surface.SurfaceMissing
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.commonSurfaceDescription
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.quests.surface.GENERIC_ROAD_SURFACES
import de.westnordost.streetcomplete.quests.surface.shouldBeDescribed
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class UniversalSurfaceOverlayForm : AbstractOverlayForm() {
    // TODO allow switching between "path surface" and "sidewalk tagged as road property surface"
    override val contentLayoutResId = R.layout.fragment_overlay_path_surface_select
    private val binding by contentViewBinding(FragmentOverlayPathSurfaceSelectBinding::bind)

    private val itemsPerRow = 3 // TODO: maybe 2 for normal user?
    /** items to display. May not be accessed before onCreate */
    val items: List<DisplayItem<Surface>> = Surface.values().filter { it !in GENERIC_ROAD_SURFACES }.map { it.asItem() }
    private val cellLayoutId: Int = R.layout.cell_icon_select_with_label_below
    private var currentStatus: SurfaceInfo? = null
    private var isSegregatedLayout = false

    private var selectedStatusForMainSurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }
    private var selectedStatusForCyclewaySurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }
    private var selectedStatusForFootwaySurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

        // TODO where this functions should be placed within file?
        override val otherAnswers: List<AnswerItem> get() {
            return if (isSegregatedLayout) {
                listOf() // removing info about separate cycleway (or maybe ven removing info about supposed cycleway!) is requiring a note
            } else {
                listOf(
                    AnswerItem(R.string.overlay_path_surface_segregated) { switchToFootwayCyclewaySurfaceLayout() }
                )
            }
        }

        private fun switchToFootwayCyclewaySurfaceLayout() {
            isSegregatedLayout = true
            binding.mainSurfaceContainer.isVisible = false
            binding.cyclewaySurfaceContainer.isVisible = true
            binding.footwaySurfaceContainer.isVisible = true
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
                        }
                        is SingleSurfaceItemWithNote -> {
                            selectedStatusForMainSurface = gathered.surface
                            binding.explanationInputMainSurface.text = SpannableStringBuilder(gathered.note)
                        }
                    }
                    checkIsFormComplete()
                }
            }
            binding.selectButtonCyclewaySurface.setOnClickListener {
                collectSurfaceData { gathered: SingleSurfaceItemInfo ->
                    when (gathered) {
                        is SingleSurfaceItem -> {
                            selectedStatusForCyclewaySurface = gathered.surface
                        }
                        is SingleSurfaceItemWithNote -> {
                            selectedStatusForCyclewaySurface = gathered.surface
                            binding.explanationInputCyclewaySurface.text = SpannableStringBuilder(gathered.note)
                        }
                    }
                    checkIsFormComplete()
                }
            }
            binding.selectButtonFootwaySurface.setOnClickListener {
                collectSurfaceData { gathered: SingleSurfaceItemInfo ->
                    when (gathered) {
                        is SingleSurfaceItem -> {
                            selectedStatusForFootwaySurface = gathered.surface
                        }
                        is SingleSurfaceItemWithNote -> {
                            selectedStatusForFootwaySurface = gathered.surface
                            binding.explanationInputFootwaySurface.text = SpannableStringBuilder(gathered.note)
                        }
                    }
                    checkIsFormComplete()
                }
            }

            if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

            LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectedCellViewMainSurface, true)
            LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectedCellViewCyclewaySurface, true)
            LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectedCellViewFootwaySurface, true)
            binding.selectedCellViewMainSurface.children.first().background = null
            binding.selectedCellViewCyclewaySurface.children.first().background = null
            binding.selectedCellViewFootwaySurface.children.first().background = null

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

            val status = createSurfaceStatus(element.tags)
            currentStatus = status
            when (status) {
                // surface=unpaved / surface=paved without note is treated as missing one
                is CyclewayFootwaySurfaces -> {
                    val cyclewaySurface = status.cycleway
                    val footwaySurface = status.footway
                    if (cyclewaySurface != null && !cyclewaySurface.shouldBeDescribed) {
                        selectedStatusForCyclewaySurface = cyclewaySurface.asItem()
                    }
                    if (footwaySurface != null && !footwaySurface.shouldBeDescribed) {
                        selectedStatusForFootwaySurface = footwaySurface.asItem()
                    }
                    switchToFootwayCyclewaySurfaceLayout()
                }
                is CyclewayFootwaySurfacesWithNote -> {
                    val cyclewaySurface = status.cycleway
                    val footwaySurface = status.footway
                    if (cyclewaySurface != null && !cyclewaySurface.shouldBeDescribed) {
                        selectedStatusForCyclewaySurface = cyclewaySurface.asItem()
                    }
                    if (footwaySurface != null && !footwaySurface.shouldBeDescribed) {
                        selectedStatusForFootwaySurface = footwaySurface.asItem()
                    }
                    binding.explanationInputMainSurface.text = SpannableStringBuilder(status.note)
                    binding.explanationInputCyclewaySurface.text = SpannableStringBuilder(status.cyclewayNote)
                    binding.explanationInputFootwaySurface.text = SpannableStringBuilder(status.footwayNote)
                    switchToFootwayCyclewaySurfaceLayout()
                }
                is SingleSurface -> {
                    val surface = status.surface
                    if (!surface.shouldBeDescribed) {
                        selectedStatusForMainSurface = status.surface.asItem()
                    }
                }
                is SingleSurfaceWithNote -> {
                    binding.explanationInputMainSurface.text = SpannableStringBuilder(status.note)
                    selectedStatusForMainSurface = status.surface.asItem()
                }
                is SurfaceMissing -> {
                    if (element.tags["segregated"] == "yes") {
                        switchToFootwayCyclewaySurfaceLayout()
                    }
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
            }

            val cyclewaySurfaceItem = selectedStatusForCyclewaySurface
            binding.selectTextViewCyclewaySurface.isGone = cyclewaySurfaceItem != null
            binding.selectedCellViewCyclewaySurface.isGone = cyclewaySurfaceItem == null
            if (cyclewaySurfaceItem != null) {
                ItemViewHolder(binding.selectedCellViewCyclewaySurface).bind(cyclewaySurfaceItem)
                binding.explanationInputCyclewaySurfaceContainer.isVisible = cyclewaySurfaceItem.value?.shouldBeDescribed ?: false
            }

            val footwaySurfaceItem = selectedStatusForFootwaySurface
            binding.selectTextViewFootwaySurface.isGone = footwaySurfaceItem != null
            binding.selectedCellViewFootwaySurface.isGone = footwaySurfaceItem == null
            if (footwaySurfaceItem != null) {
                ItemViewHolder(binding.selectedCellViewFootwaySurface).bind(footwaySurfaceItem)
                binding.explanationInputFootwaySurfaceContainer.isVisible = footwaySurfaceItem.value?.shouldBeDescribed ?: false
            }
        }

        /* ------------------------------------- instance state ------------------------------------- */

        private fun onLoadInstanceState(inState: Bundle) {
            val selectedMainSurfaceIndex = inState.getInt(SELECTED_MAIN_SURFACE_INDEX)
            val selectedMainSurfaceNoteText = inState.getString(SELECTED_MAIN_SURFACE_NOTE_TEXT)
            val selectedCyclewaySurfaceIndex = inState.getInt(SELECTED_CYCLEWAY_SURFACE_INDEX)
            val selectedCyclewaySurfaceNoteText = inState.getString(SELECTED_CYCLEWAY_SURFACE_NOTE_TEXT)
            val selectedFootwaySurfaceIndex = inState.getInt(SELECTED_FOOTWAY_SURFACE_INDEX)
            val selectedFootwaySurfaceNoteText = inState.getString(SELECTED_FOOTWAY_SURFACE_NOTE_TEXT)
            selectedStatusForMainSurface = if (selectedMainSurfaceIndex != -1) items[selectedMainSurfaceIndex] else null
            binding.explanationInputMainSurface.text = SpannableStringBuilder(selectedMainSurfaceNoteText)
            selectedStatusForCyclewaySurface = if (selectedCyclewaySurfaceIndex != -1) items[selectedCyclewaySurfaceIndex] else null
            binding.explanationInputCyclewaySurface.text = SpannableStringBuilder(selectedCyclewaySurfaceNoteText)
            selectedStatusForFootwaySurface = if (selectedFootwaySurfaceIndex != -1) items[selectedFootwaySurfaceIndex] else null
            binding.explanationInputFootwaySurface.text = SpannableStringBuilder(selectedFootwaySurfaceNoteText)
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt(SELECTED_MAIN_SURFACE_INDEX, items.indexOf(selectedStatusForMainSurface))
            outState.putString(SELECTED_MAIN_SURFACE_NOTE_TEXT, noteText())
            outState.putInt(SELECTED_CYCLEWAY_SURFACE_INDEX, items.indexOf(selectedStatusForCyclewaySurface))
            outState.putString(SELECTED_CYCLEWAY_SURFACE_NOTE_TEXT, noteText())
            outState.putInt(SELECTED_FOOTWAY_SURFACE_INDEX, items.indexOf(selectedStatusForFootwaySurface))
            outState.putString(SELECTED_FOOTWAY_SURFACE_NOTE_TEXT, noteText())
        }

        /* -------------------------------------- apply answer -------------------------------------- */

        override fun isFormComplete(): Boolean {
            if (selectedStatusForMainSurface == null) {
                return selectedStatusForCyclewaySurface != null && selectedStatusForFootwaySurface != null
            }
            val surfaceValue = selectedStatusForMainSurface!!.value
            val note = noteText()
            if (surfaceValue == null) {
                return false
            }
            if (surfaceValue.shouldBeDescribed) {
                return note != ""
            }
            return true
        }

        companion object {
        private const val SELECTED_MAIN_SURFACE_INDEX = "selected_main_surface_index"
        private const val SELECTED_MAIN_SURFACE_NOTE_TEXT = "selected_main_surface_note_text"
        private const val SELECTED_CYCLEWAY_SURFACE_INDEX = "selected_cycleway_surface_index"
        private const val SELECTED_CYCLEWAY_SURFACE_NOTE_TEXT = "selected_cycleway_surface_index_note_text" // this also needs support! TODO
        private const val SELECTED_FOOTWAY_SURFACE_INDEX = "selected_footway_surface_index"
        private const val SELECTED_FOOTWAY_SURFACE_NOTE_TEXT = "selected_footway_surface_index_note_text" // this also needs support! TODO
    }

    fun noteText(): String {
        return binding.explanationInputMainSurface.text.toString().trim()
    }

    fun cyclewayNoteText(): String {
        return binding.explanationInputCyclewaySurface.text.toString().trim()
    }

    fun footwayNoteText(): String {
        return binding.explanationInputFootwaySurface.text.toString().trim()
    }

    override fun hasChanges(): Boolean {
        return when (val status = currentStatus) {
            is CyclewayFootwaySurfaces ->
                selectedStatusForCyclewaySurface?.value != status.cycleway || selectedStatusForFootwaySurface?.value != status.footway
            is SingleSurface -> selectedStatusForMainSurface?.value != status.surface
            is SingleSurfaceWithNote -> selectedStatusForMainSurface?.value != status.surface || noteText() != status.note
            is SurfaceMissing -> selectedStatusForMainSurface?.value != null || selectedStatusForCyclewaySurface?.value != null || selectedStatusForFootwaySurface?.value != null
            is CyclewayFootwaySurfacesWithNote -> {
                selectedStatusForMainSurface?.value != status.main || noteText() != status.note ||
                selectedStatusForCyclewaySurface?.value != status.main || cyclewayNoteText() != status.cyclewayNote ||
                selectedStatusForFootwaySurface?.value != status.main || footwayNoteText() != status.footwayNote
            }
            null -> throw Exception("it was supposed to be set in onViewCreated - is it possible to trigger it before onViewCreated completes?")
        }
    }

    override fun onClickOk() {
        if (selectedStatusForCyclewaySurface != null && selectedStatusForFootwaySurface != null) {
            val cyclewaySurface = selectedStatusForCyclewaySurface!!.value!!.osmValue
            val footwaySurface = selectedStatusForFootwaySurface!!.value!!.osmValue
            // TODO: support cycleway:surface:note
            // TODO: support footway:surface:note
            val mainSurface = commonSurfaceDescription(cyclewaySurface, footwaySurface)
            applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
                if (mainSurface == null) {
                    it.remove("surface")
                    it.updateWithCheckDate("cycleway:surface", cyclewaySurface)
                    it.updateWithCheckDate("footway:surface", footwaySurface)
                } else {
                    it.updateWithCheckDate("surface", mainSurface)
                    it.updateWithCheckDate("cycleway:surface", cyclewaySurface)
                    it.updateWithCheckDate("footway:surface", footwaySurface)
                }
            }.create()))
        } else {
            // like RoadSurfaceOverlayForm is doing this
            val note = noteText()
            val surfaceObject = selectedStatusForMainSurface!!.value!!
            applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
                it.updateWithCheckDate("surface", surfaceObject.osmValue)
                if (surfaceObject.shouldBeDescribed) {
                    it["surface:note"] = note
                } else {
                    if (element.tags.containsKey("surface:note")) {
                        it.remove("surface:note")
                    }
                }
            }.create()))
        }
    }
}
