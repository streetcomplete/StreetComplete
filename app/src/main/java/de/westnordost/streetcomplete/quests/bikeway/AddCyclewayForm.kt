package de.westnordost.streetcomplete.quests.bikeway

import android.os.Bundle
import androidx.annotation.AnyThread
import android.view.View

import java.util.Collections

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.view.dialogs.ImageListPickerDialog
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*


class AddCyclewayForm : AbstractQuestFormAnswerFragment<CyclewayAnswer>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle
    override val contentPadding = false

    override val otherAnswers: List<OtherAnswer> get() {
        val isNoRoundabout = osmElement!!.tags["junction"] != "roundabout"
        val result = mutableListOf<OtherAnswer>()
        if (!isDefiningBothSides && isNoRoundabout) {
            result.add(OtherAnswer(R.string.quest_cycleway_answer_contraflow_cycleway) { showBothSides() })
        }
        return result
    }

    private val likelyNoBicycleContraflow = FiltersParser().parse("""
            ways with oneway:bicycle != no and
            (oneway ~ yes|-1 and highway ~ primary|secondary|tertiary or junction=roundabout)
        """)

    private var streetSideRotater: StreetSideRotater? = null

    private var isDefiningBothSides: Boolean = false

    private var leftSide: Cycleway? = null
    private var rightSide: Cycleway? = null

    /** returns whether the side that goes into the opposite direction of the driving direction of a
     * one-way is on the right side of the way */
    private val isReverseSideRight get() = isReversedOneway xor isLeftHandTraffic

    private val isOneway get() = isForwardOneway || isReversedOneway

    private val isForwardOneway get() = osmElement!!.tags["oneway"] == "yes"
    private val isReversedOneway get() = osmElement!!.tags["oneway"] == "-1"

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isDefiningBothSides = savedInstanceState?.getBoolean(DEFINE_BOTH_SIDES)
                ?: !likelyNoBicycleContraflow.matches(osmElement!!)

        savedInstanceState?.getString(CYCLEWAY_RIGHT)?.let { rightSide = Cycleway.valueOf(it) }
        savedInstanceState?.getString(CYCLEWAY_LEFT)?.let { leftSide = Cycleway.valueOf(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        puzzleView.listener = { isRight -> showCyclewaySelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedleView, elementGeometry as ElementPolylinesGeometry)

        if (!isDefiningBothSides) {
            if (isLeftHandTraffic) puzzleView.showOnlyLeftSide()
            else                   puzzleView.showOnlyRightSide()
        }

        val defaultResId =
            if (isLeftHandTraffic) R.drawable.ic_cycleway_unknown_l
            else                   R.drawable.ic_cycleway_unknown

        puzzleView.setLeftSideImageResource(leftSide?.getIconResId(isLeftHandTraffic) ?: defaultResId)
        puzzleView.setRightSideImageResource(rightSide?.getIconResId(isLeftHandTraffic) ?: defaultResId)

        checkIsFormComplete()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rightSide?.let { outState.putString(CYCLEWAY_RIGHT, it.name) }
        leftSide?.let { outState.putString(CYCLEWAY_LEFT, it.name) }
        outState.putBoolean(DEFINE_BOTH_SIDES, isDefiningBothSides)
    }

    @AnyThread
    override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    override fun onClickOk() {
        val leftSide = leftSide
        val rightSide = rightSide

        // a cycleway that goes into opposite direction of a oneway street needs special tagging
        var leftSideDir = 0
        var rightSideDir = 0
        var isOnewayNotForCyclists = false
        if (isOneway && leftSide != null && rightSide != null) {
            // if the road is oneway=-1, a cycleway that goes opposite to it would be cycleway:oneway=yes
            val reverseDir = if (isReversedOneway) 1 else -1

            if (isReverseSideRight) {
                if (rightSide.isSingleTrackOrLane()) {
                    rightSideDir = reverseDir
                }
            } else {
                if (leftSide.isSingleTrackOrLane()) {
                    leftSideDir = reverseDir
                }
            }

            isOnewayNotForCyclists = leftSide.isDualTrackOrLane() || rightSide.isDualTrackOrLane()
                    || (if(isReverseSideRight) rightSide else leftSide) !== Cycleway.NONE
        }

        applyAnswer(CyclewayAnswer(
            left = leftSide?.let { CyclewaySide(it, leftSideDir) },
            right = rightSide?.let { CyclewaySide(it, rightSideDir) },
            isOnewayNotForCyclists = isOnewayNotForCyclists
        ))
    }

    private fun Cycleway.isSingleTrackOrLane() =
        this === Cycleway.TRACK || this === Cycleway.EXCLUSIVE_LANE

    private fun Cycleway.isDualTrackOrLane() =
        this === Cycleway.DUAL_TRACK || this === Cycleway.DUAL_LANE

    override fun isFormComplete() =
        if (isDefiningBothSides) leftSide != null && rightSide != null
        else                     leftSide != null || rightSide != null

    override fun isRejectingClose() = leftSide != null || rightSide != null

    private fun showCyclewaySelectionDialog(isRight: Boolean) {
        val ctx = context ?: return
        val items = getCyclewayItems(isRight).map { it.asItem(isLeftHandTraffic) }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2) { selected ->
            val cycleway = selected.value!!
            val iconResId = cycleway.getIconResId(isLeftHandTraffic)

            if (isRight) {
                puzzleView.replaceRightSideImageResource(iconResId)
                rightSide = cycleway
            } else {
                puzzleView.replaceLeftSideImageResource(iconResId)
                leftSide = cycleway
            }
            checkIsFormComplete()
        }.show()
    }

    private fun getCyclewayItems(isRight: Boolean): List<Cycleway> {
        val values = Cycleway.displayValues.toMutableList()
        // different wording for a contraflow lane that is marked like a "shared" lane (just bicycle pictogram)
        if (isOneway && isReverseSideRight == isRight) {
            Collections.replaceAll(values, Cycleway.PICTOGRAMS, Cycleway.NONE_NO_ONEWAY)
        }
        val country = countryInfo.countryCode
        if ("BE" == country) {
            // Belgium does not make a difference between continuous and dashed lanes -> so don't tag that difference
            // also, in Belgium there is a differentiation between the normal lanes and suggestion lanes
            values.remove(Cycleway.EXCLUSIVE_LANE)
            values.remove(Cycleway.ADVISORY_LANE)
            values.add(0, Cycleway.LANE_UNSPECIFIED)
            values.add(1, Cycleway.SUGGESTION_LANE)
        } else if ("NL" == country) {
            // a differentiation between dashed lanes and suggestion lanes only exist in NL and BE
            values.add(values.indexOf(Cycleway.ADVISORY_LANE) + 1, Cycleway.SUGGESTION_LANE)
        }

        return values
    }

    private fun showBothSides() {
        isDefiningBothSides = true
        puzzleView.showBothSides()
        checkIsFormComplete()
    }

    companion object {
        private const val CYCLEWAY_LEFT = "cycleway_left"
        private const val CYCLEWAY_RIGHT = "cycleway_right"
        private const val DEFINE_BOTH_SIDES = "define_both_sides"
    }
}
