package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.BOTH
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.LEFT
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.RIGHT
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class AddSidewalkSurfaceForm : AStreetSideSelectForm<Surface, SidewalkSurfaceAnswer>() {

    private var leftNote: String? = null
    private var rightNote: String? = null

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_sidewalk_answer_different) { applyAnswer(SidewalkIsDifferent) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        }
    }

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
            hasLeft && hasRight -> BOTH
            hasLeft -> LEFT
            hasRight -> RIGHT
            else -> return
        }
    }

    override fun onClickSide(isRight: Boolean) {
        val items = (PAVED_SURFACES + UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_ROAD_SURFACES)
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
        applyAnswer(SidewalkSurface(
            left?.let { SurfaceAnswer(it, leftNote) },
            right?.let { SurfaceAnswer(it, rightNote) }
        ))
    }

    /* ------------------------------------- instance state ------------------------------------- */

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        leftNote = savedInstanceState.getString(LEFT_NOTE, null)
        rightNote = savedInstanceState.getString(RIGHT_NOTE, null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LEFT_NOTE, leftNote)
        outState.putString(RIGHT_NOTE, rightNote)
    }

    /* ------------------------------------------------------------------------------------------ */

    override fun serialize(item: Surface) = item.name
    override fun deserialize(str: String) = Surface.valueOf(str)
    override fun asStreetSideItem(item: Surface, isRight: Boolean) =
        item.asStreetSideItem(resources)

    companion object {
        private const val LEFT_NOTE = "left_note"
        private const val RIGHT_NOTE = "right_note"
    }
}
