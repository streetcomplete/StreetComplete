package de.westnordost.streetcomplete.quests.surface

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem
import de.westnordost.streetcomplete.quests.StreetSideItem
import de.westnordost.streetcomplete.quests.StreetSideItem2
import de.westnordost.streetcomplete.quests.sidewalk.imageResId
import de.westnordost.streetcomplete.quests.sidewalk.titleResId
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText

class AddSidewalkSurfaceForm : AStreetSideSelectFragment<SurfaceAnswer, SidewalkSurfaceAnswer>() {

    override val items =
        (PAVED_SURFACES + UNPAVED_SURFACES + Surface.WOODCHIPS + GROUND_SURFACES + GENERIC_ROAD_SURFACES).map {
            StreetSideItem(
                SurfaceAnswer(it),
                R.drawable.ic_sidewalk_illustration_yes,
                it.asItem().titleId,
                it.asItem().drawableId!!,
                it.asItem().drawableId
            )
        }

    private val currentSidewalks get() = createSidewalkSides(osmElement!!.tags)

    private var isDefiningBothSides: Boolean = false
    private var isLeftSideNotDefined: Boolean = false
    private var isRightSideNotDefined: Boolean = false

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

    override fun onClickOk(leftSide: SurfaceAnswer, rightSide: SurfaceAnswer) {
        applyAnswer(SidewalkSurfaceAnswer(leftSide, rightSide))
    }
}
