package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem
import de.westnordost.streetcomplete.quests.StreetSideItem2
import de.westnordost.streetcomplete.quests.sidewalk.imageResId
import de.westnordost.streetcomplete.quests.sidewalk.titleResId
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable

class AddSidewalkSurfaceForm : AStreetSideSelectFragment<Surface, SidewalkSurfaceAnswer>() {

    override val items: List<Surface> get() =
        PAVED_SURFACES + UNPAVED_SURFACES + Surface.WOODCHIPS + GROUND_SURFACES + GENERIC_ROAD_SURFACES

    private var leftNote: String? = null
    private var rightNote: String? = null

    override fun getDisplayItem(value: Surface): StreetSideDisplayItem<Surface> {
        val it = value.asItem()
        return StreetSideItem2(
            value,
            ResImage(R.drawable.ic_sidewalk_illustration_yes),
            ResText(it.titleId!!),
            ResImage(it.drawableId!!),
            DrawableImage(RotatedCircleDrawable(resources.getDrawable(it.drawableId)))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            onLoadInstanceState(savedInstanceState)
        }
    }

    override fun initStateFromTags() {
        val sides = createSidewalkSides(osmElement!!.tags)
        val left = sides?.left
        val right = sides?.right

        if (left != null && right != null) {
            isDefiningBothSides = (left == Sidewalk.YES) && (right == Sidewalk.YES)
            if (right == Sidewalk.NO || right == Sidewalk.SEPARATE) {
                puzzleView?.setRightSideText(ResText(right.titleResId))
                puzzleView?.setRightSideImage(ResImage(right.imageResId))
                puzzleView?.setOnlyLeftSideClickable()
            }
            if (left == Sidewalk.NO || left == Sidewalk.SEPARATE) {
                puzzleView?.setLeftSideText(ResText(left.titleResId))
                puzzleView?.setLeftSideImage(ResImage(left.imageResId))
                puzzleView?.setOnlyRightSideClickable()
            }
        }
    }

    /** only save previous selection if both sides were filled and if none needs an additional
     *  description (because that description is not shown in the preview - in fact, nowhere, in the
     *  UI) */
    override fun shouldSaveSelection(left: Surface?, right: Surface?): Boolean =
        left != null && right != null && !left.shouldBeDescribed && !right.shouldBeDescribed

    override fun onSelectedSide(selection: Surface, isRight: Boolean) {
        if (selection.shouldBeDescribed) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    DescribeGenericSurfaceDialog(requireContext()) { description ->
                        setNote(description, isRight)
                        replaceSide(selection, isRight)
                    }.show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            setNote(null, isRight)
            replaceSide(selection, isRight)
        }
    }

    private fun setNote(note: String?, isRight: Boolean) {
        if (isRight) rightNote = note
        else leftNote = note
    }

    override fun onClickOk(leftSide: Surface?, rightSide: Surface?) {
        applyAnswer(SidewalkSurfaceAnswer(
            leftSide?.let { SurfaceAnswer(it, leftNote) },
            rightSide?.let { SurfaceAnswer(it, rightNote) }
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

    companion object {
        private const val LEFT_NOTE = "left_note"
        private const val RIGHT_NOTE = "right_note"
    }
}
