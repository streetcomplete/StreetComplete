package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.asItem
import de.westnordost.streetcomplete.osm.sidewalk.asStreetSideItem
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.overlays.AStreetSideSelectOverlayForm
import de.westnordost.streetcomplete.quests.surface.COMMON_SPECIFIC_PAVED_SURFACES
import de.westnordost.streetcomplete.quests.surface.COMMON_SPECIFIC_UNPAVED_SURFACES
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.quests.surface.GENERIC_ROAD_SURFACES
import de.westnordost.streetcomplete.quests.surface.GROUND_SURFACES
import de.westnordost.streetcomplete.quests.surface.SidewalkSurfaceAnswer
import de.westnordost.streetcomplete.quests.surface.SurfaceAnswer
import de.westnordost.streetcomplete.quests.surface.asItem
import de.westnordost.streetcomplete.quests.surface.asStreetSideItem
import de.westnordost.streetcomplete.quests.surface.shouldBeDescribed
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class SidewalkSurfaceOverlayForm : AStreetSideSelectOverlayForm<Surface>() {
    private var currentSidewalk: LeftAndRightSidewalk? = null
    private var leftNote: String? = null
    private var rightNote: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        val sides = createSidewalkSides(element.tags)
        val hasLeft = sides?.left == Sidewalk.YES
        val hasRight = sides?.right == Sidewalk.YES

        streetSideSelect.showSides = when {
            hasLeft && hasRight -> StreetSideSelectWithLastAnswerButtonViewController.Sides.BOTH
            hasLeft -> StreetSideSelectWithLastAnswerButtonViewController.Sides.LEFT
            hasRight -> StreetSideSelectWithLastAnswerButtonViewController.Sides.RIGHT
            else -> return
        }
        // actually init surface....
        val leftSurfaceString = element.tags["sidewalk:both:surface"] ?: element.tags["sidewalk:left:surface"]
        val rightSurfaceString = element.tags["sidewalk:both:surface"] ?: element.tags["sidewalk:right:surface"]
        val leftSurfaceObject = Surface.values().find { it.osmValue == leftSurfaceString }
        val rightSurfaceObject = Surface.values().find { it.osmValue == rightSurfaceString }
        if(leftSurfaceObject != null) {
            streetSideSelect.replacePuzzleSide(leftSurfaceObject.asStreetSideItem(resources), false)
        }
        if(rightSurfaceObject != null) {
            streetSideSelect.replacePuzzleSide(rightSurfaceObject.asStreetSideItem(resources), true)
        }
    }

    override fun onClickSide(isRight: Boolean) {
        val items = (COMMON_SPECIFIC_PAVED_SURFACES + COMMON_SPECIFIC_UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_ROAD_SURFACES)
            .map { it.asItem() }

        ImageListPickerDialog(requireContext(), items, R.layout.cell_icon_select_with_label_below, 2) { item ->
            val streetSideItem = item.value!!.asStreetSideItem(requireContext().resources)

            if (item.value?.shouldBeDescribed == true) {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                    .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                        DescribeGenericSurfaceDialog(requireContext()) { description ->
                            setNote(isRight, description)
                            streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
                        }.show()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            } else {
                setNote(isRight, null)
                streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
            }
        }.show()
    }

    private fun setNote(isRight: Boolean, note: String?) {
        if (isRight) rightNote = note
        else leftNote = note
    }

    override fun onClickOk() {
        val left = streetSideSelect.left?.value
        val right = streetSideSelect.right?.value
        if (left?.shouldBeDescribed != true && right?.shouldBeDescribed != true) {
            streetSideSelect.saveLastSelection()
        }
        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also {
            SidewalkSurfaceAnswer(
                left?.let { leftSurface -> SurfaceAnswer(leftSurface, leftNote) },
                right?.let { rightSurface -> SurfaceAnswer(rightSurface, rightNote) }
            ).applyTo(it)
        }.create()))
    }

    /* ------------------------------------- instance state ------------------------------------- */

    fun onLoadInstanceState(savedInstanceState: Bundle) {
        leftNote = savedInstanceState.getString(LEFT_NOTE, null)
        rightNote = savedInstanceState.getString(RIGHT_NOTE, null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LEFT_NOTE, leftNote)
        outState.putString(RIGHT_NOTE, rightNote)
    }

    override fun hasChanges(): Boolean =
        streetSideSelect.left?.value != currentSidewalk?.left ||
            streetSideSelect.right?.value != currentSidewalk?.right

    override fun serialize(item: StreetSideDisplayItem<Surface>, isRight: Boolean) =
        item.value.name

    override fun deserialize(str: String, isRight: Boolean) =
        Surface.valueOf(str).asStreetSideItem(resources)

    companion object {
        private const val LEFT_NOTE = "left_note"
        private const val RIGHT_NOTE = "right_note"
    }
}
