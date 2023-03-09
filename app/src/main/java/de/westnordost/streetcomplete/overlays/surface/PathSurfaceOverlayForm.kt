package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.databinding.FragmentOverlayPathSurfaceSelectBinding
import de.westnordost.streetcomplete.osm.surface.SELECTABLE_WAY_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.createSurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.shouldBeDescribed
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.osm.surface.updateCommonSurfaceFromFootAndCyclewaySurface
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.util.getFeatureName
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class PathSurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_path_surface_select
    private val binding by contentViewBinding(FragmentOverlayPathSurfaceSelectBinding::bind)

    private val noteText get() = binding.main.explanationInput.nonBlankTextOrNull
    private val cyclewayNoteText get() = binding.cycleway.explanationInput.nonBlankTextOrNull
    private val footwayNoteText get() = binding.footway.explanationInput.nonBlankTextOrNull

    private val items: List<DisplayItem<Surface>> = SELECTABLE_WAY_SURFACES.toItems()
    private val cellLayoutId: Int = R.layout.cell_labeled_icon_select

    private var originalSurface: SurfaceAndNote? = null
    private var originalFootwaySurface: SurfaceAndNote? = null
    private var originalCyclewaySurface: SurfaceAndNote? = null

    private var isSegregatedLayout = false

    private var selectedSurfaceItem: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCellMain()
        }
    private var selectedCyclewaySurfaceItem: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCellCycleway()
        }
    private var selectedFootwaySurfaceItem: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCellFootway()
        }

    override val otherAnswers: List<IAnswerItem> get() =
        if (isSegregatedLayout) {
            /*
            No option to switch back to single surface. Removing info about separate cycleway is
            too complicated.

            Typically it requires editing not only surface info but also an access info as it
            happens in cases where bicycle access is gone. May require also removal of
            cycleway=separate, bicycle=use_sidepath from the road.

            And in cases where there is a segregated cycleway with the same surface as footway
            then StreetComplete will anyway ask for cycleway:surface and footway:surface.

            Fortunately need for this change are really rare. Notes can be left as usual.
            */
            listOf()

        } else if (isBothFootAndBicycleTraffic(element!!)) {
            /*
            Only where bicycle access is already present because adding bicycle access typically
            requires adding proper access tags, interconnections with roads and often also other
            geometry changes.

            In case where path is not clearly marked as carrying both foot and bicycle traffic
            mapper can leave a note
            */
            listOf(
                AnswerItem(R.string.overlay_path_surface_segregated) {
                    // reset previous data
                    selectedSurfaceItem = null
                    binding.main.explanationInput.text = null
                    switchToFootwayCyclewaySurfaceLayout()
                }
            )
        } else {
            listOf()
        }

    private val isBothFootAndBicycleTrafficFilter by lazy { """
        ways, relations with
          highway = footway and bicycle ~ yes|designated
          or highway = cycleway and foot ~ yes|designated
          or highway = path and foot != no and bicycle != no
    """.toElementFilterExpression() }

    private fun isBothFootAndBicycleTraffic(element: Element): Boolean =
        isBothFootAndBicycleTrafficFilter.matches(element)

    private fun switchToFootwayCyclewaySurfaceLayout() {
        isSegregatedLayout = true
        binding.main.root.isGone = true
        binding.cyclewaySurfaceContainer.isGone = false
        binding.footwaySurfaceContainer.isGone = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        originalSurface = createSurfaceAndNote(element!!.tags)
        originalCyclewaySurface = createSurfaceAndNote(element!!.tags, "cycleway")
        originalFootwaySurface = createSurfaceAndNote(element!!.tags, "footway")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.main.explanationInput.doAfterTextChanged { checkIsFormComplete() }
        binding.footway.explanationInput.doAfterTextChanged { checkIsFormComplete() }
        binding.cycleway.explanationInput.doAfterTextChanged { checkIsFormComplete() }

        binding.main.selectButton.root.setOnClickListener {
            collectSurfaceData { surface: Surface, note: String? ->
                selectedSurfaceItem = surface.asItem()
                binding.main.explanationInput.setText(note)
                checkIsFormComplete()
            }
        }
        binding.cycleway.selectButton.root.setOnClickListener {
            collectSurfaceData { surface: Surface, note: String? ->
                selectedCyclewaySurfaceItem = surface.asItem()
                binding.cycleway.explanationInput.setText(note)
                checkIsFormComplete()
            }
        }
        binding.footway.selectButton.root.setOnClickListener {
            collectSurfaceData { surface: Surface, note: String? ->
                selectedFootwaySurfaceItem = surface.asItem()
                binding.footway.explanationInput.setText(note)
                checkIsFormComplete()
            }
        }

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.main.selectButton.selectedCellView, true)
        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.cycleway.selectButton.selectedCellView, true)
        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.footway.selectButton.selectedCellView, true)

        binding.main.selectButton.root.children.first().background = null
        binding.cycleway.selectButton.root.children.first().background = null
        binding.footway.selectButton.root.children.first().background = null

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        } else {
            initStateFromTags()
        }

        if (element!!.tags["segregated"] == "yes" || originalCyclewaySurface != null || originalFootwaySurface != null) {
            switchToFootwayCyclewaySurfaceLayout()
        }

        val conf = resources.configuration
        binding.cyclewaySurfaceLabel.text = featureDictionary.getFeatureName(conf, mapOf("highway" to "cycleway"), GeometryType.LINE)
        binding.footwaySurfaceLabel.text = featureDictionary.getFeatureName(conf, mapOf("highway" to "footway"), GeometryType.LINE)

        updateSelectedCellMain()
        updateSelectedCellCycleway()
        updateSelectedCellFootway()
    }

    private fun initStateFromTags() {
        selectedSurfaceItem = originalSurface?.value?.asItem()
        selectedCyclewaySurfaceItem = originalCyclewaySurface?.value?.asItem()
        selectedFootwaySurfaceItem = originalFootwaySurface?.value?.asItem()

        binding.main.explanationInput.setText(originalSurface?.note)
        binding.cycleway.explanationInput.setText(originalCyclewaySurface?.note)
        binding.footway.explanationInput.setText(originalFootwaySurface?.note)
    }

    private fun collectSurfaceData(callback: (Surface, String?) -> Unit) {
        ImageListPickerDialog(requireContext(), items, cellLayoutId, 2) { item ->
            val value = item.value
            if (value != null && value.shouldBeDescribed) {
                DescribeGenericSurfaceDialog(requireContext()) { description ->
                    callback(item.value!!, description)
                }.show()
            } else {
                callback(item.value!!, null)
            }
        }.show()
    }

    private fun updateSelectedCellMain() {
        val mainItem = selectedSurfaceItem
        binding.main.selectButton.selectTextView.isGone = mainItem != null
        binding.main.selectButton.selectedCellView.isGone = mainItem == null
        if (mainItem != null) {
            ItemViewHolder(binding.main.selectButton.selectedCellView).bind(mainItem)
        }
        if (noteText != null || mainItem?.value?.shouldBeDescribed == true) {
            binding.main.explanationInput.isGone = false
            binding.main.root.isGone = false
        }
    }

    private fun updateSelectedCellCycleway() {
        val cyclewayItem = selectedCyclewaySurfaceItem
        binding.cycleway.selectButton.selectTextView.isGone = cyclewayItem != null
        binding.cycleway.selectButton.selectedCellView.isGone = cyclewayItem == null
        if (cyclewayItem != null) {
            ItemViewHolder(binding.cycleway.selectButton.selectedCellView).bind(cyclewayItem)
        }
        if (cyclewayNoteText != null || cyclewayItem?.value?.shouldBeDescribed == true) {
            binding.cycleway.explanationInput.isGone = false
        }
    }

    private fun updateSelectedCellFootway() {
        val footwayItem = selectedFootwaySurfaceItem
        binding.footway.selectButton.selectTextView.isGone = footwayItem != null
        binding.footway.selectButton.selectedCellView.isGone = footwayItem == null
        if (footwayItem != null) {
            ItemViewHolder(binding.footway.selectButton.selectedCellView).bind(footwayItem)
        }
        if (footwayNoteText != null || footwayItem?.value?.shouldBeDescribed == true) {
            binding.footway.explanationInput.isGone = false
        }
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        selectedSurfaceItem = items.getOrNull(inState.getInt(SELECTED_MAIN_INDEX, -1))
        binding.main.explanationInput.setText(inState.getString(MAIN_NOTE_TEXT))

        selectedCyclewaySurfaceItem = items.getOrNull(inState.getInt(SELECTED_CYCLEWAY_INDEX, -1))
        binding.cycleway.explanationInput.setText(inState.getString(CYCLEWAY_NOTE_TEXT))

        selectedFootwaySurfaceItem = items.getOrNull(inState.getInt(SELECTED_FOOTWAY_INDEX, -1))
        binding.footway.explanationInput.setText(inState.getString(FOOTWAY_NOTE_TEXT))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_MAIN_INDEX, items.indexOf(selectedSurfaceItem))
        outState.putString(MAIN_NOTE_TEXT, noteText)
        outState.putInt(SELECTED_CYCLEWAY_INDEX, items.indexOf(selectedCyclewaySurfaceItem))
        outState.putString(CYCLEWAY_NOTE_TEXT, noteText)
        outState.putInt(SELECTED_FOOTWAY_INDEX, items.indexOf(selectedFootwaySurfaceItem))
        outState.putString(FOOTWAY_NOTE_TEXT, noteText)
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete(): Boolean {
        if (isSegregatedLayout) {
            val cyclewaySurface = selectedCyclewaySurfaceItem?.value
            val footwaySurface = selectedFootwaySurfaceItem?.value

            return cyclewaySurface != null && (!cyclewaySurface.shouldBeDescribed || cyclewayNoteText != null)
                && footwaySurface != null && (!footwaySurface.shouldBeDescribed || footwayNoteText != null)
        } else {
            val surface = selectedSurfaceItem?.value

            return surface != null && (!surface.shouldBeDescribed || noteText != null)
        }
    }

    override fun hasChanges(): Boolean =
        selectedSurfaceItem?.value != originalSurface?.value ||
        selectedCyclewaySurfaceItem?.value != originalCyclewaySurface?.value ||
        selectedFootwaySurfaceItem?.value != originalFootwaySurface?.value ||
        noteText != originalSurface?.note ||
        cyclewayNoteText != originalCyclewaySurface?.note ||
        footwayNoteText != originalFootwaySurface?.note

    override fun onClickOk() {
        val changesBuilder = StringMapChangesBuilder(element!!.tags)

        if (isSegregatedLayout) {
            val cyclewaySurface = selectedCyclewaySurfaceItem!!.value!!
            val footwaySurface = selectedFootwaySurfaceItem!!.value!!

            changesBuilder["segregated"] = "yes"
            SurfaceAndNote(footwaySurface, footwayNoteText).applyTo(changesBuilder, "footway")
            SurfaceAndNote(cyclewaySurface, cyclewayNoteText).applyTo(changesBuilder, "cycleway")
            updateCommonSurfaceFromFootAndCyclewaySurface(changesBuilder)
        } else {
            val surface = selectedSurfaceItem!!.value!!
            SurfaceAndNote(surface, noteText).applyTo(changesBuilder)
        }

        applyEdit(UpdateElementTagsAction(changesBuilder.create()))
    }

    companion object {
        private const val SELECTED_MAIN_INDEX = "selected_main_index"
        private const val MAIN_NOTE_TEXT = "main_note_text"
        private const val SELECTED_CYCLEWAY_INDEX = "selected_cycleway_index"
        private const val CYCLEWAY_NOTE_TEXT = "cycleway_note_text"
        private const val SELECTED_FOOTWAY_INDEX = "selected_footway_index"
        private const val FOOTWAY_NOTE_TEXT = "footway_note_text"
    }
}
