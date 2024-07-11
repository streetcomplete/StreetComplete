package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.FragmentOverlaySurfaceSelectBinding
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.surface.SELECTABLE_WAY_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.isComplete
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.osm.surface.parseSurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.updateCommonSurfaceFromFootAndCyclewaySurface
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.util.getLanguagesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.couldBeSteps
import de.westnordost.streetcomplete.util.ktx.valueOfOrNull
import de.westnordost.streetcomplete.view.setImage
import org.koin.android.ext.android.inject

class SurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_surface_select
    private val binding by contentViewBinding(FragmentOverlaySurfaceSelectBinding::bind)

    private val prefs: Preferences by inject()

    private val lastPickedSurface: Surface? get() =
        prefs.getLastPicked(this::class.simpleName!!)
            .map { valueOfOrNull<Surface>(it) }
            .firstOrNull()

    private lateinit var surfaceCtrl: SurfaceAndNoteViewController
    private lateinit var cyclewaySurfaceCtrl: SurfaceAndNoteViewController
    private lateinit var footwaySurfaceCtrl: SurfaceAndNoteViewController

    private var originalSurface: SurfaceAndNote? = null
    private var originalFootwaySurface: SurfaceAndNote? = null
    private var originalCyclewaySurface: SurfaceAndNote? = null

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

        originalSurface = parseSurfaceAndNote(element!!.tags)
        originalCyclewaySurface = parseSurfaceAndNote(element!!.tags, "cycleway")
        originalFootwaySurface = parseSurfaceAndNote(element!!.tags, "footway")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surfaceCtrl = SurfaceAndNoteViewController(
            binding.main.selectButton.root,
            binding.main.explanationInput,
            binding.main.selectButton.selectedCellView,
            binding.main.selectButton.selectTextView,
            SELECTABLE_WAY_SURFACES
        )
        surfaceCtrl.onInputChanged = { checkIsFormComplete() }

        cyclewaySurfaceCtrl = SurfaceAndNoteViewController(
            binding.cycleway.selectButton.root,
            binding.cycleway.explanationInput,
            binding.cycleway.selectButton.selectedCellView,
            binding.cycleway.selectButton.selectTextView,
            SELECTABLE_WAY_SURFACES
        )
        cyclewaySurfaceCtrl.onInputChanged = { checkIsFormComplete() }

        footwaySurfaceCtrl = SurfaceAndNoteViewController(
            binding.footway.selectButton.root,
            binding.footway.explanationInput,
            binding.footway.selectButton.selectedCellView,
            binding.footway.selectButton.selectTextView,
            SELECTABLE_WAY_SURFACES
        )
        footwaySurfaceCtrl.onInputChanged = { checkIsFormComplete() }

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        } else {
            initStateFromTags()
        }

        binding.lastPickedButton.isGone = lastPickedSurface == null
        binding.lastPickedButton.setImage(lastPickedSurface?.asItem()?.image)
        binding.lastPickedButton.setOnClickListener {
            surfaceCtrl.value = SurfaceAndNote(lastPickedSurface)
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
        surfaceCtrl.value = originalSurface
        cyclewaySurfaceCtrl.value = originalCyclewaySurface
        footwaySurfaceCtrl.value = originalFootwaySurface
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        surfaceCtrl.value = SurfaceAndNote(
            parseSurface(inState.getString(SURFACE)),
            inState.getString(NOTE)
        )
        cyclewaySurfaceCtrl.value = SurfaceAndNote(
            parseSurface(inState.getString(CYCLEWAY_SURFACE)),
            inState.getString(CYCLEWAY_NOTE)
        )
        footwaySurfaceCtrl.value = SurfaceAndNote(
            parseSurface(inState.getString(FOOTWAY_SURFACE)),
            inState.getString(FOOTWAY_NOTE)
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(SURFACE, surfaceCtrl.value?.surface?.osmValue)
        outState.putString(NOTE, surfaceCtrl.value?.note)

        outState.putString(CYCLEWAY_SURFACE, cyclewaySurfaceCtrl.value?.surface?.osmValue)
        outState.putString(CYCLEWAY_NOTE, cyclewaySurfaceCtrl.value?.note)

        outState.putString(FOOTWAY_SURFACE, footwaySurfaceCtrl.value?.surface?.osmValue)
        outState.putString(FOOTWAY_NOTE, footwaySurfaceCtrl.value?.note)
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete(): Boolean =
        if (isSegregatedLayout) {
            cyclewaySurfaceCtrl.value?.isComplete == true
            && footwaySurfaceCtrl.value?.isComplete == true
        } else {
            surfaceCtrl.value?.isComplete == true
        }

    override fun hasChanges(): Boolean =
        surfaceCtrl.value != originalSurface ||
        cyclewaySurfaceCtrl.value != originalCyclewaySurface ||
        footwaySurfaceCtrl.value != originalFootwaySurface

    override fun onClickOk() {
        val changesBuilder = StringMapChangesBuilder(element!!.tags)

        if (isSegregatedLayout) {
            changesBuilder["segregated"] = "yes"
            cyclewaySurfaceCtrl.value!!.applyTo(changesBuilder, "cycleway")
            footwaySurfaceCtrl.value!!.applyTo(changesBuilder, "footway")
            updateCommonSurfaceFromFootAndCyclewaySurface(changesBuilder)
        } else {
            if (surfaceCtrl.value!!.note == null && surfaceCtrl.value!!.surface != null) {
                prefs.addLastPicked(this::class.simpleName!!, surfaceCtrl.value!!.surface!!.name)
            }
            surfaceCtrl.value!!.applyTo(changesBuilder)
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
                surfaceCtrl.value = originalSurface
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
        private const val NOTE = "note"
        private const val CYCLEWAY_SURFACE = "cycleway_surface"
        private const val CYCLEWAY_NOTE = "cycleway_note"
        private const val FOOTWAY_SURFACE = "footway_surface"
        private const val FOOTWAY_NOTE = "footway_note"
    }
}
