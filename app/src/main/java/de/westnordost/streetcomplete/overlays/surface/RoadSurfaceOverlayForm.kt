package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.databinding.FragmentOverlayRoadSurfaceSelectBinding
import de.westnordost.streetcomplete.osm.surface.SELECTABLE_WAY_SURFACES
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.createSurface
import de.westnordost.streetcomplete.osm.surface.createSurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.isComplete
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm

class RoadSurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_road_surface_select
    private val containerBinding by contentViewBinding(FragmentOverlayRoadSurfaceSelectBinding::bind)
    private val binding get() = containerBinding.form

    private lateinit var surfaceCtrl: SurfaceAndNoteViewController

    private var originalSurface: SurfaceAndNote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        originalSurface = createSurfaceAndNote(element!!.tags)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        surfaceCtrl = SurfaceAndNoteViewController(
            binding.selectButton.root,
            binding.explanationInput,
            binding.selectButton.selectedCellView,
            binding.selectButton.selectTextView,
            SELECTABLE_WAY_SURFACES
        )
        surfaceCtrl.onInputChanged = { checkIsFormComplete() }

        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        } else {
            initStateFromTags()
        }

        checkIsFormComplete()
    }

    private fun initStateFromTags() {
        surfaceCtrl.value = originalSurface
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(inState: Bundle) {
        surfaceCtrl.value = SurfaceAndNote(
            createSurface(inState.getString(SURFACE)),
            inState.getString(NOTE)
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SURFACE, surfaceCtrl.value?.surface?.osmValue)
        outState.putString(NOTE, surfaceCtrl.value?.note)
    }

    /* -------------------------------------- apply answer -------------------------------------- */

    override fun isFormComplete(): Boolean = surfaceCtrl.value?.isComplete == true

    override fun hasChanges(): Boolean = surfaceCtrl.value != originalSurface

    override fun onClickOk() {
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        surfaceCtrl.value!!.applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(tagChanges.create()))
    }

    companion object {
        private const val SURFACE = "surface"
        private const val NOTE = "note"
    }
}
