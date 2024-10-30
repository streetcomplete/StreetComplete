package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.FragmentOverlaySurfaceSelectBinding
import de.westnordost.streetcomplete.databinding.ViewImageSelectBinding
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.surface.SELECTABLE_WAY_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.osm.surface.updateCommonSurfaceFromFootAndCyclewaySurface
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder
import de.westnordost.streetcomplete.view.setImage
import org.koin.android.ext.android.inject

class SurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_surface_select
    private val binding by contentViewBinding(FragmentOverlaySurfaceSelectBinding::bind)

    private val prefs: Preferences by inject()

    private val selectableItems: List<DisplayItem<Surface>> get() =
        SELECTABLE_WAY_SURFACES.map { it.asItem() }

    private val lastPickedSurface: Surface? get() =
        prefs.getLastPicked(this::class.simpleName!!)
            .map { valueOfOrNull<Surface>(it) }
            .firstOrNull()

    private var originalSurface: Surface? = null
    private var originalFootwaySurface: Surface? = null
    private var originalCyclewaySurface: Surface? = null

    private var selectedSurface: Surface? = null
        set(value) {
            field = value
            updateSelectedCell(binding.main, value)
        }
    private var selectedFootwaySurface: Surface? = null
        set(value) {
            field = value
            updateSelectedCell(binding.footway, value)
        }
    private var selectedCyclewaySurface: Surface? = null
        set(value) {
            field = value
            updateSelectedCell(binding.cycleway, value)
        }

    private val cellLayoutId: Int = R.layout.cell_labeled_image_select

    private var isSegregatedLayout = false

    override val otherAnswers: List<IAnswerItem> get() = listOfNotNull(
        createSegregatedAnswer(),
        createConvertToStepsAnswer()
    )

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
        binding.lastPickedButton.isGone = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tags = element!!.tags
        originalSurface = parseSurface(tags["surface"])
        originalCyclewaySurface = parseSurface(tags["cycleway:surface"])
        originalFootwaySurface = parseSurface(tags["footway:surface"])
    }

    private fun updateSelectedCell(cellBinding: ViewImageSelectBinding, item: Surface?) {
        cellBinding.selectTextView.isGone = item != null
        cellBinding.selectedCellView.isGone = item == null
        if (item != null) {
            ItemViewHolder(cellBinding.selectedCellView).bind(item.asItem())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.main.selectedCellView, true)
        binding.main.selectedCellView.children.first().background = null
        binding.main.selectButton.setOnClickListener {
            ImageListPickerDialog(requireContext(), selectableItems, cellLayoutId) { item ->
                if (item.value != selectedSurface) {
                    selectedSurface = item.value
                    checkIsFormComplete()
                }
            }.show()
        }

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.cycleway.selectedCellView, true)
        binding.cycleway.selectedCellView.children.first().background = null
        binding.cycleway.selectButton.setOnClickListener {
            ImageListPickerDialog(requireContext(), selectableItems, cellLayoutId) { item ->
                if (item.value != selectedCyclewaySurface) {
                    selectedCyclewaySurface = item.value
                    checkIsFormComplete()
                }
            }.show()
        }

        LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.footway.selectedCellView, true)
        binding.footway.selectedCellView.children.first().background = null
        binding.footway.selectButton.setOnClickListener {
            ImageListPickerDialog(requireContext(), selectableItems, cellLayoutId) { item ->
                if (item.value != selectedFootwaySurface) {
                    selectedFootwaySurface = item.value
                    checkIsFormComplete()
                }
            }.show()
        }

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        } else {
            initStateFromTags()
        }

        binding.lastPickedButton.isGone = lastPickedSurface == null
        binding.lastPickedButton.setImage(lastPickedSurface?.asItem()?.image)
        binding.lastPickedButton.setOnClickListener {
            selectedSurface = lastPickedSurface
            binding.lastPickedButton.isGone = true
            checkIsFormComplete()
        }

        val isSegregated = element!!.tags["segregated"] == "yes"
        val isPath = element!!.tags["highway"] in ALL_PATHS
        if (isPath && (isSegregated || originalCyclewaySurface != null || originalFootwaySurface != null)) {
            switchToFootwayCyclewaySurfaceLayout()
        }

        val languages = getLanguagesForFeatureDictionary(resources.configuration)
        binding.cyclewaySurfaceLabel.text =
            featureDictionary.getById("highway/cycleway", languages = languages)?.name
        binding.footwaySurfaceLabel.text =
            featureDictionary.getById("highway/footway", languages = languages)?.name

        checkIsFormComplete()
    }

    private fun initStateFromTags() {
        selectedSurface = originalSurface
        selectedCyclewaySurface = originalCyclewaySurface
        selectedFootwaySurface = originalFootwaySurface
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        selectedSurface = parseSurface(inState.getString(SURFACE))
        selectedCyclewaySurface = parseSurface(inState.getString(CYCLEWAY_SURFACE))
        selectedFootwaySurface = parseSurface(inState.getString(FOOTWAY_SURFACE))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SURFACE, selectedSurface?.osmValue)
        outState.putString(CYCLEWAY_SURFACE, selectedCyclewaySurface?.osmValue)
        outState.putString(FOOTWAY_SURFACE, selectedFootwaySurface?.osmValue)
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete(): Boolean =
        if (isSegregatedLayout) {
            selectedCyclewaySurface != null && selectedFootwaySurface != null
        } else {
            selectedSurface != null
        }

    override fun hasChanges(): Boolean =
        selectedSurface != originalSurface ||
        selectedCyclewaySurface != originalCyclewaySurface ||
        selectedFootwaySurface != originalFootwaySurface

    override fun onClickOk() {
        val changesBuilder = StringMapChangesBuilder(element!!.tags)

        if (isSegregatedLayout) {
            changesBuilder["segregated"] = "yes"
            selectedCyclewaySurface?.applyTo(changesBuilder, "cycleway")
            selectedFootwaySurface?.applyTo(changesBuilder, "footway")
            updateCommonSurfaceFromFootAndCyclewaySurface(changesBuilder)
        } else {
            selectedSurface?.let { prefs.addLastPicked(this::class.simpleName!!, it.name) }
            selectedSurface?.applyTo(changesBuilder)
        }

        applyEdit(UpdateElementTagsAction(element!!, changesBuilder.create()))
    }

    private fun createSegregatedAnswer(): AnswerItem? =
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
            null
        } else if (isBothFootAndBicycleTraffic(element!!)) {
            /*
                Only where bicycle access is already present because adding bicycle access typically
                requires adding proper access tags, interconnections with roads and often also other
                geometry changes.

                In case where path is not clearly marked as carrying both foot and bicycle traffic
                mapper can leave a note
             */
            AnswerItem(R.string.overlay_path_surface_segregated) {
                // reset previous data
                selectedSurface = originalSurface
                switchToFootwayCyclewaySurfaceLayout()
            }
        } else {
            null
        }

    private fun createConvertToStepsAnswer(): AnswerItem? =
        if (element!!.couldBeSteps()) {
            AnswerItem(R.string.quest_generic_answer_is_actually_steps) { changeToSteps() }
        } else {
            null
        }

    private fun changeToSteps() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        tagChanges.changeToSteps()
        applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
    }

    companion object {
        private const val SURFACE = "selected_surface"
        private const val CYCLEWAY_SURFACE = "cycleway_surface"
        private const val FOOTWAY_SURFACE = "footway_surface"
    }
}
