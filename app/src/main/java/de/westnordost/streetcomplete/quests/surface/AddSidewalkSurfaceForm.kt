package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.LastSelection
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem
import de.westnordost.streetcomplete.quests.StreetSideItem2
import de.westnordost.streetcomplete.quests.sidewalk.imageResId
import de.westnordost.streetcomplete.quests.sidewalk.titleResId
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable

class AddSidewalkSurfaceForm : AStreetSideSelectFragment<SurfaceAnswer, SidewalkSurfaceAnswer>() {

    override val items: List<StreetSideDisplayItem<SurfaceAnswer>>
        get() = getDisplayItems()

    private fun getDisplayItems(): List<StreetSideDisplayItem<SurfaceAnswer>> {
        return (PAVED_SURFACES + UNPAVED_SURFACES + Surface.WOODCHIPS + GROUND_SURFACES + GENERIC_ROAD_SURFACES).map {
            StreetSideItem2(
                SurfaceAnswer(it),
                ResImage(R.drawable.ic_sidewalk_illustration_yes),
                it.asItem().titleId?.let { it1 -> ResText(it1) },
                ResImage(it.asItem().drawableId!!),
                DrawableImage(RotatedCircleDrawable(resources.getDrawable(it.asItem().drawableId!!)))
            )
        }
    }

    private val currentSidewalks get() = createSidewalkSides(osmElement!!.tags)

    override fun initStateFromTags() {
        val left = currentSidewalks?.left
        val right = currentSidewalks?.right

        if (left != null && right != null) {
            isDefiningBothSides = (left == Sidewalk.YES) && (right == Sidewalk.YES)
            isRightSideNotDefined = (right == Sidewalk.NO) || (right == Sidewalk.SEPARATE)
            isLeftSideNotDefined = (left == Sidewalk.NO) || (left == Sidewalk.SEPARATE)
            if (right == Sidewalk.NO || right == Sidewalk.SEPARATE) {
                puzzleView?.setRightSideText(ResText(right.titleResId))
                puzzleView?.setRightSideImage(ResImage(right.imageResId))
                puzzleView?.onlyLeftSideClickable()
            }
            if (left == Sidewalk.NO || left == Sidewalk.SEPARATE) {
                puzzleView?.setLeftSideText(ResText(left.titleResId))
                puzzleView?.setLeftSideImage(ResImage(left.imageResId))
                puzzleView?.onlyRightSideClickable()
            }
        }
    }

    override fun sideFollowUpQuestion(selection: StreetSideDisplayItem<SurfaceAnswer>, isRight: Boolean) {
        val surface = selection.value.value
        if (surface.shouldBeDescribed) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.quest_surface_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                    DescribeGenericSurfaceDialog(requireContext()) { description ->
                        onSelectedSide(StreetSideItem2(
                            SurfaceAnswer(surface, description),
                            selection.image,
                            selection.title,
                            selection.icon,
                            selection.floatingIcon), isRight)
                    }.show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            onSelectedSide(selection, isRight)
        }
    }

    override fun onClickOk(leftSide: SurfaceAnswer?, rightSide: SurfaceAnswer?) {
        applyAnswer(SidewalkSurfaceAnswer(leftSide, rightSide))
    }

    override fun serializeAnswer(answer: LastSelection<SurfaceAnswer>): String {
        return "${answer.left.value.value}#${answer.left.value.note}#${answer.right.value.value}#${answer.right.value.note}"
    }

    override fun deserializeAnswer(str: String): LastSelection<SurfaceAnswer> {
        val arr = str.split('#')

        val leftSurface = Surface.values().find { it.toString() == arr[0] }!!
        val leftNote = if (arr[1] == "null") {
            null
        } else {
            arr[1]
        }
        val leftStreetSideItem = StreetSideItem2(
            SurfaceAnswer(leftSurface, leftNote),
            ResImage(R.drawable.ic_sidewalk_illustration_yes),
            leftSurface.asItem().titleId?.let { ResText(it) },
            ResImage(leftSurface.asItem().drawableId!!),
            DrawableImage(RotatedCircleDrawable(resources.getDrawable(leftSurface.asItem().drawableId!!)))
        )

        val rightSurface = Surface.values().find { it.toString() == arr[2] }!!
        val rightNote = if (arr[3] == "null") {
            null
        } else {
            arr[3]
        }
        val rightStreetSideItem = StreetSideItem2(
            SurfaceAnswer(rightSurface, rightNote),
            ResImage(R.drawable.ic_sidewalk_illustration_yes),
            rightSurface.asItem().titleId?.let { ResText(it) },
            ResImage(rightSurface.asItem().drawableId!!),
            DrawableImage(RotatedCircleDrawable(resources.getDrawable(rightSurface.asItem().drawableId!!)))
        )

        return LastSelection(leftStreetSideItem, rightStreetSideItem)
    }
}
