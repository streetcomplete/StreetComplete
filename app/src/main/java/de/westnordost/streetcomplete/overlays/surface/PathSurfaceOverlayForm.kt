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
import de.westnordost.streetcomplete.databinding.FragmentOverlayPathSurfaceSelectBinding
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfaces
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfacesWithNote
import de.westnordost.streetcomplete.osm.surface.SingleSurface
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAnswer
import de.westnordost.streetcomplete.osm.surface.SurfaceInfo
import de.westnordost.streetcomplete.osm.surface.SurfaceMissing
import de.westnordost.streetcomplete.osm.surface.SurfaceMissingWithNote
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.commonSurfaceObject
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.surface.COMMON_SPECIFIC_PAVED_SURFACES
import de.westnordost.streetcomplete.quests.surface.COMMON_SPECIFIC_UNPAVED_SURFACES
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.quests.surface.GENERIC_AREA_SURFACES
import de.westnordost.streetcomplete.quests.surface.GROUND_SURFACES
import de.westnordost.streetcomplete.quests.surface.shouldBeDescribed
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class PathSurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_path_surface_select
    private val binding by contentViewBinding(FragmentOverlayPathSurfaceSelectBinding::bind)

    private val itemsPerRow = 2
    /** items to display. May not be accessed before onCreate */
    val items: List<DisplayItem<Surface>> = (COMMON_SPECIFIC_PAVED_SURFACES + COMMON_SPECIFIC_UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_AREA_SURFACES).toItems()
    private val cellLayoutId: Int = R.layout.cell_icon_select_with_label_below
    private var originalSurfaceStatus: SurfaceInfo? = null
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

        override val otherAnswers: List<AnswerItem> get() {
            return if (isSegregatedLayout) {
                listOf() // removing info about separate cycleway (or maybe ven removing info about supposed cycleway!) is requiring a note
            } else {
                listOf(
                    AnswerItem(R.string.overlay_path_surface_segregated) {
                        // reset previous data
                        selectedStatusForMainSurface = null
                        binding.explanationInputMainSurface.text = null
                        binding.selectButtonMainSurface.isVisible = false
                        switchToFootwayCyclewaySurfaceLayout()
                    }
                )
            }
        }

        private fun switchToFootwayCyclewaySurfaceLayout() {
            isSegregatedLayout = true
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

            binding.explanationInputMainSurface.doAfterTextChanged { checkIsFormComplete() }
            binding.explanationInputFootwaySurface.doAfterTextChanged { checkIsFormComplete() }
            binding.explanationInputCyclewaySurface.doAfterTextChanged { checkIsFormComplete() }

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
            binding.selectButtonCyclewaySurface.setOnClickListener {
                collectSurfaceData { gathered: SingleSurfaceItemInfo ->
                    when (gathered) {
                        is SingleSurfaceItem -> {
                            selectedStatusForCyclewaySurface = gathered.surface
                            binding.explanationInputCyclewaySurface.text = null
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
                            binding.explanationInputFootwaySurface.text = null
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

            val status = createSurfaceStatus(element!!.tags)
            originalSurfaceStatus = status
            when (status) {
                // surface=unpaved / surface=paved without note is treated as missing one
                is CyclewayFootwaySurfaces -> {
                    val cyclewaySurface = status.cycleway
                    val footwaySurface = status.footway
                    if (cyclewaySurface != null) {
                        selectedStatusForCyclewaySurface = cyclewaySurface.asItem()
                    }
                    if (footwaySurface != null) {
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
                    if (status.note != null) {
                        binding.explanationInputMainSurface.text = SpannableStringBuilder(status.note)
                    }
                    if (status.cyclewayNote != null) {
                        binding.explanationInputCyclewaySurface.text = SpannableStringBuilder(status.cyclewayNote)
                        selectedStatusForCyclewaySurface = cyclewaySurface?.asItem() // even if paved/unpaved
                    }
                    if (status.footwayNote != null) {
                        binding.explanationInputFootwaySurface.text = SpannableStringBuilder(status.footwayNote)
                        selectedStatusForFootwaySurface = footwaySurface?.asItem() // even if paved/unpaved
                    }
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
                    selectedStatusForMainSurface = status.surface.asItem() // even if paved/unpaved
                }
                is SurfaceMissing -> {
                    if (element!!.tags["segregated"] == "yes") {
                        switchToFootwayCyclewaySurfaceLayout()
                    }
                }
                is SurfaceMissingWithNote -> {
                    binding.explanationInputMainSurface.text = SpannableStringBuilder(status.note)
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
            if (noteText() != null || mainSurfaceItem?.value?.shouldBeDescribed == true) {
                binding.explanationInputMainSurfaceContainer.isVisible = true
                binding.mainSurfaceContainer.isVisible = true
            }

            val cyclewaySurfaceItem = selectedStatusForCyclewaySurface
            binding.selectTextViewCyclewaySurface.isGone = cyclewaySurfaceItem != null
            binding.selectedCellViewCyclewaySurface.isGone = cyclewaySurfaceItem == null
            if (cyclewaySurfaceItem != null) {
                ItemViewHolder(binding.selectedCellViewCyclewaySurface).bind(cyclewaySurfaceItem)
            }
            if (cyclewayNoteText() != null || cyclewaySurfaceItem?.value?.shouldBeDescribed == true) {
                binding.explanationInputCyclewaySurfaceContainer.isVisible = true
            }

            val footwaySurfaceItem = selectedStatusForFootwaySurface
            binding.selectTextViewFootwaySurface.isGone = footwaySurfaceItem != null
            binding.selectedCellViewFootwaySurface.isGone = footwaySurfaceItem == null
            if (footwaySurfaceItem != null) {
                ItemViewHolder(binding.selectedCellViewFootwaySurface).bind(footwaySurfaceItem)
            }
            if (footwayNoteText() != null || footwaySurfaceItem?.value?.shouldBeDescribed == true) {
                binding.explanationInputFootwaySurfaceContainer.isVisible = true
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
            if (selectedMainSurfaceNoteText != null) {
                binding.explanationInputMainSurface.text = SpannableStringBuilder(selectedMainSurfaceNoteText)
            }
            selectedStatusForCyclewaySurface = if (selectedCyclewaySurfaceIndex != -1) items[selectedCyclewaySurfaceIndex] else null
            if (selectedCyclewaySurfaceNoteText != null) {
                binding.explanationInputCyclewaySurface.text = SpannableStringBuilder(selectedCyclewaySurfaceNoteText)
            }
            selectedStatusForFootwaySurface = if (selectedFootwaySurfaceIndex != -1) items[selectedFootwaySurfaceIndex] else null
            if (selectedFootwaySurfaceNoteText != null) {
                binding.explanationInputFootwaySurface.text = SpannableStringBuilder(selectedFootwaySurfaceNoteText)
            }
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
            // should we allow editing surface:note?
            // also where surface is not specified (maybe even surface=paved/unpaved cannot be really specified)
            // so saving is allowed even in absense of surface
            if (isAnyOriginallyExistingNotesEdited()) {
                return true
            }
            if (selectedStatusForMainSurface == null) {
                if (selectedStatusForCyclewaySurface == null || selectedStatusForFootwaySurface == null) {
                    return false
                }
            }
            return hasChanges()
        }

    private fun isAnyOriginallyExistingNotesEdited(): Boolean {
        when (val original = originalSurfaceStatus) {
            is SingleSurfaceWithNote -> {
                if (noteText() != original.note) {
                    return true
                }
            }
            is SurfaceMissingWithNote -> {
                if (noteText() != original.note) {
                    return true
                }
            }
            is CyclewayFootwaySurfacesWithNote -> {
                if (noteText() != original.note) {
                    return true
                }
                if (cyclewayNoteText() != original.cyclewayNote) {
                    return true
                }
                if (footwayNoteText() != original.footwayNote) {
                    return true
                }
            }
            else -> {
                return false
            }
        }
        return false
    }

    fun noteText(): String? {
        return binding.explanationInputMainSurface.nonBlankTextOrNull
    }

    private fun cyclewayNoteText(): String? {
        return binding.explanationInputCyclewaySurface.nonBlankTextOrNull
    }

    private fun footwayNoteText(): String? {
        return binding.explanationInputFootwaySurface.nonBlankTextOrNull
    }

    override fun hasChanges(): Boolean {
        return when (val original = originalSurfaceStatus) {
            is CyclewayFootwaySurfaces ->
                selectedStatusForCyclewaySurface?.value != original.cycleway || selectedStatusForFootwaySurface?.value != original.footway
            is SingleSurface -> selectedStatusForMainSurface?.value != original.surface
                || selectedStatusForCyclewaySurface?.value != null || selectedStatusForFootwaySurface?.value != null
            is SingleSurfaceWithNote -> selectedStatusForMainSurface?.value != original.surface || noteText() != original.note
                || selectedStatusForCyclewaySurface?.value != null || selectedStatusForFootwaySurface?.value != null
            is SurfaceMissing -> selectedStatusForMainSurface?.value != null
                || selectedStatusForCyclewaySurface?.value != null || selectedStatusForFootwaySurface?.value != null
            is SurfaceMissingWithNote -> selectedStatusForMainSurface?.value != null  || noteText() != original.note
                || selectedStatusForCyclewaySurface?.value != null || selectedStatusForFootwaySurface?.value != null
            is CyclewayFootwaySurfacesWithNote -> {
                // selectedStatusForMainSurface?.value != original.main
                // is not being checked as surface is dropped and will be derived from cycleway & footway surface
                noteText() != original.note ||
                selectedStatusForCyclewaySurface?.value != original.cycleway || cyclewayNoteText() != original.cyclewayNote ||
                selectedStatusForFootwaySurface?.value != original.footway || footwayNoteText() != original.footwayNote
            }
            null -> throw Exception("it was supposed to be set in onViewCreated - is it possible to trigger it before onViewCreated completes?")
        }
    }

    override fun onClickOk() {
        if (selectedStatusForCyclewaySurface != null && selectedStatusForFootwaySurface != null) {
            val cyclewaySurface = selectedStatusForCyclewaySurface!!.value!!
            val footwaySurface = selectedStatusForFootwaySurface!!.value!!
            applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element!!.tags).also {
                editTagsWithSeparateCyclewayAndFootwayAnswer(it, cyclewaySurface, cyclewayNoteText(), footwaySurface, footwayNoteText(), noteText())
            }.create()))
        } else {
            // like RoadSurfaceOverlayForm is doing this
            val surfaceObject = selectedStatusForMainSurface!!.value!!
            applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element!!.tags).also {
                editTagsWithMainSurfaceAnswer(it, surfaceObject, noteText())
            }.create()))
        }
    }

    companion object {
        fun editTagsWithMainSurfaceAnswer(changesBuilder: StringMapChangesBuilder, surfaceObject: Surface, note: String?) {
            SurfaceAnswer(surfaceObject, note).applyTo(changesBuilder)
        }

        fun editTagsWithSeparateCyclewayAndFootwayAnswer(changesBuilder: StringMapChangesBuilder, cyclewaySurface: Surface, cyclewayNote: String?, footwaySurface: Surface, footwayNote: String?, generalSurfaceNote: String?) {
            val mainSurface = commonSurfaceObject(cyclewaySurface.osmValue, footwaySurface.osmValue)
            if (mainSurface == null) {
                if (changesBuilder.containsKey("surface")) {
                    changesBuilder.remove("surface")
                }
                if (changesBuilder.containsKey("surface:note") && generalSurfaceNote == null) {
                    changesBuilder.remove("surface:note")
                }
                if (generalSurfaceNote != null && changesBuilder["surface:note"] != generalSurfaceNote) {
                    changesBuilder["surface:note"] = generalSurfaceNote
                }
                SurfaceAnswer(footwaySurface, footwayNote).applyTo(changesBuilder, prefix = "footway")
                SurfaceAnswer(cyclewaySurface, cyclewayNote).applyTo(changesBuilder, prefix = "cycleway")
            } else {
                SurfaceAnswer(mainSurface, generalSurfaceNote).applyTo(changesBuilder)
                SurfaceAnswer(footwaySurface, footwayNote).applyTo(changesBuilder, prefix = "footway")
                SurfaceAnswer(cyclewaySurface, cyclewayNote).applyTo(changesBuilder, prefix = "cycleway")
            }
        }

        private const val SELECTED_MAIN_SURFACE_INDEX = "selected_main_surface_index"
        private const val SELECTED_MAIN_SURFACE_NOTE_TEXT = "selected_main_surface_note_text"
        private const val SELECTED_CYCLEWAY_SURFACE_INDEX = "selected_cycleway_surface_index"
        private const val SELECTED_CYCLEWAY_SURFACE_NOTE_TEXT = "selected_cycleway_surface_index_note_text"
        private const val SELECTED_FOOTWAY_SURFACE_INDEX = "selected_footway_surface_index"
        private const val SELECTED_FOOTWAY_SURFACE_NOTE_TEXT = "selected_footway_surface_index_note_text"
    }
}
