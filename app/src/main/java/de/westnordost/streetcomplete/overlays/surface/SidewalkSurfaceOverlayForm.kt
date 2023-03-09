package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.LeftAndRightSidewalkSurface
import de.westnordost.streetcomplete.osm.sidewalk_surface.applyTo
import de.westnordost.streetcomplete.osm.sidewalk_surface.createSidewalkSurface
import de.westnordost.streetcomplete.osm.surface.SELECTABLE_WAY_SURFACES
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.asStreetSideItem
import de.westnordost.streetcomplete.osm.surface.shouldBeDescribed
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.overlays.AStreetSideSelectOverlayForm
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class SidewalkSurfaceOverlayForm : AStreetSideSelectOverlayForm<Surface>() {

    private var originalSidewalkSurface: LeftAndRightSidewalkSurface? = null
    private var leftNote: String? = null
    private var rightNote: String? = null

    private val items: List<DisplayItem<Surface>> = SELECTABLE_WAY_SURFACES.toItems()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalSidewalkSurface = createSidewalkSurface(element!!.tags)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        } else {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        val sidewalk = createSidewalkSides(element!!.tags)
        val hasLeft = sidewalk?.left == Sidewalk.YES
        val hasRight = sidewalk?.right == Sidewalk.YES

        streetSideSelect.showSides = when {
            hasLeft && hasRight -> Sides.BOTH
            hasLeft -> Sides.LEFT
            hasRight -> Sides.RIGHT
            else -> return
        }
        // actually init surface....
        streetSideSelect.setPuzzleSide(originalSidewalkSurface?.left?.value?.asStreetSideItem(resources), false)
        streetSideSelect.setPuzzleSide(originalSidewalkSurface?.right?.value?.asStreetSideItem(resources), true)
        leftNote = originalSidewalkSurface?.left?.note
        rightNote = originalSidewalkSurface?.right?.note
    }

    override fun onClickSide(isRight: Boolean) {
        ImageListPickerDialog(requireContext(), items, R.layout.cell_labeled_icon_select, 2) { item ->
            val surface = item.value!!
            if (surface.shouldBeDescribed) {
                showDescribeSurfaceDialog(isRight, surface)
            } else {
                replaceSurfaceSide(isRight, surface, null)
            }
        }.show()
    }

    private fun showDescribeSurfaceDialog(isRight: Boolean, surface: Surface) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                DescribeGenericSurfaceDialog(requireContext()) { description ->
                    replaceSurfaceSide(isRight, surface, description)
                }.show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun replaceSurfaceSide(isRight: Boolean, surface: Surface, description: String?) {
        val streetSideItem = surface.asStreetSideItem(requireContext().resources)
        if (isRight) {
            rightNote = description
        } else {
            leftNote = description
        }
        streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
    }

    /* ------------------------------------- apply changes  ------------------------------------- */

    override fun isFormComplete(): Boolean {
        val left = streetSideSelect.left?.value
        val right = streetSideSelect.left?.value
        return left != null && (!left.shouldBeDescribed || leftNote != null) &&
               right != null && (!right.shouldBeDescribed || rightNote != null)
    }

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != originalSidewalkSurface?.left?.value ||
        streetSideSelect.right?.value != originalSidewalkSurface?.right?.value ||
        leftNote != originalSidewalkSurface?.left?.note ||
        rightNote != originalSidewalkSurface?.right?.note

    override fun onClickOk() {
        val left = streetSideSelect.left?.value
        val right = streetSideSelect.right?.value
        // only save last selection if it is a value that does not need to be described with a note
        if (left?.shouldBeDescribed != true && right?.shouldBeDescribed != true) {
            streetSideSelect.saveLastSelection()
        }
        val tagChanges = StringMapChangesBuilder(element!!.tags)
        LeftAndRightSidewalkSurface(
            left?.let { SurfaceAndNote(it, leftNote) },
            right?.let { SurfaceAndNote(it, rightNote) }
        ).applyTo(tagChanges)
        applyEdit(UpdateElementTagsAction(tagChanges.create()))
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        leftNote = savedInstanceState.getString(LEFT_NOTE, null)
        rightNote = savedInstanceState.getString(RIGHT_NOTE, null)
        // street side select puzzle is initialized in superclass
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LEFT_NOTE, leftNote)
        outState.putString(RIGHT_NOTE, rightNote)
    }

    override fun serialize(item: Surface): String = item.name
    override fun deserialize(str: String): Surface = Surface.valueOf(str)
    override fun asStreetSideItem(item: Surface, isRight: Boolean) = item.asStreetSideItem(resources)

    companion object {
        private const val LEFT_NOTE = "left_note"
        private const val RIGHT_NOTE = "right_note"
    }
}
