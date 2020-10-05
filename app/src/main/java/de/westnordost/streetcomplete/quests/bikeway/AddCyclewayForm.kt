package de.westnordost.streetcomplete.quests.bikeway

import android.os.Bundle
import androidx.annotation.AnyThread
import android.view.View

import java.util.Collections

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.elementfilter.ElementFiltersParser
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*
import kotlinx.android.synthetic.main.view_little_compass.*

class AddCyclewayForm : AbstractQuestFormAnswerFragment<CyclewayAnswer>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle
    override val contentPadding = false

    private var isDisplayingPreviousCycleway: Boolean = false

    override val otherAnswers: List<OtherAnswer> get() {
        val isNoRoundabout = osmElement!!.tags["junction"] != "roundabout"
        val result = mutableListOf<OtherAnswer>()
        if (!isDefiningBothSides && isNoRoundabout) {
            result.add(OtherAnswer(R.string.quest_cycleway_answer_contraflow_cycleway) { showBothSides() })
        }
        return result
    }

    private val likelyNoBicycleContraflow = ElementFiltersParser().parse("""
            ways with oneway:bicycle != no and (
                oneway ~ yes|-1 and highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
                or junction = roundabout
            )
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            initStateFromTags()
        } else {
            onLoadInstanceState(savedInstanceState)
        }

        puzzleView.listener = { isRight -> showCyclewaySelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedleView, elementGeometry as ElementPolylinesGeometry)

        if (!isDefiningBothSides) {
            if (isLeftHandTraffic) puzzleView.showOnlyLeftSide()
            else                   puzzleView.showOnlyRightSide()
        }

        val defaultResId =
            if (isLeftHandTraffic) R.drawable.ic_cycleway_unknown_l
            else                   R.drawable.ic_cycleway_unknown

        val defaultTitleId = R.string.quest_street_side_puzzle_select

        puzzleView.setLeftSideImageResource(leftSide?.getIconResId(isLeftHandTraffic) ?: defaultResId)
        puzzleView.setRightSideImageResource(rightSide?.getIconResId(isLeftHandTraffic) ?: defaultResId)
        puzzleView.setLeftSideText(resources.getString(leftSide?.getTitleResId() ?: defaultTitleId ))
        puzzleView.setRightSideText(resources.getString(rightSide?.getTitleResId() ?: defaultTitleId ))

        checkIsFormComplete()
    }

    private fun initStateFromTags() {
        val countryCode = countryInfo.countryCode
        val sides = createCyclewaySides(osmElement!!.tags, isLeftHandTraffic)
        val left = sides?.left?.takeIf { it.isAvailableAsSelection(countryCode) }
        val right = sides?.right?.takeIf { it.isAvailableAsSelection(countryCode) }
        val bothSidesWereDefinedBefore = sides?.left != null && sides.right != null

        leftSide = left
        rightSide = right
        isDefiningBothSides = bothSidesWereDefinedBefore || !likelyNoBicycleContraflow.matches(osmElement!!)

        // only show as re-survey (yes/no button) if the previous tagging was complete
        setAsResurvey(isFormComplete())
    }

    private fun onLoadInstanceState(savedInstanceState: Bundle) {
        isDefiningBothSides = savedInstanceState.getBoolean(DEFINE_BOTH_SIDES)
        savedInstanceState.getString(CYCLEWAY_RIGHT)?.let { rightSide = Cycleway.valueOf(it) }
        savedInstanceState.getString(CYCLEWAY_LEFT)?.let { leftSide = Cycleway.valueOf(it) }
        isDisplayingPreviousCycleway = savedInstanceState.getBoolean(IS_DISPLAYING_PREVIOUS_CYCLEWAY)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rightSide?.let { outState.putString(CYCLEWAY_RIGHT, it.name) }
        leftSide?.let { outState.putString(CYCLEWAY_LEFT, it.name) }
        outState.putBoolean(DEFINE_BOTH_SIDES, isDefiningBothSides)
        outState.putBoolean(IS_DISPLAYING_PREVIOUS_CYCLEWAY, isDisplayingPreviousCycleway)

    }

    private fun setAsResurvey(resurvey: Boolean) {
        isDisplayingPreviousCycleway = resurvey
        puzzleView.isEnabled = !resurvey
        if (resurvey) {
            setButtonsView(R.layout.quest_buttonpanel_yes_no)
            requireView().findViewById<View>(R.id.noButton).setOnClickListener {
                setAsResurvey(false)
            }
            requireView().findViewById<View>(R.id.yesButton).setOnClickListener {
                onClickOk()
            }
        } else {
            removeButtonsView()
        }
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

    override fun isFormComplete() = !isDisplayingPreviousCycleway && (
        if (isDefiningBothSides) leftSide != null && rightSide != null
        else                     leftSide != null || rightSide != null
    )

    override fun isRejectingClose() =
        !isDisplayingPreviousCycleway && (leftSide != null || rightSide != null)

    private fun showCyclewaySelectionDialog(isRight: Boolean) {
        val ctx = context ?: return
        val items = getCyclewayItems(isRight).map { it.asItem(isLeftHandTraffic) }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 2) { selected ->
            val cycleway = selected.value!!
            val iconResId = cycleway.getIconResId(isLeftHandTraffic)
            val titleResId = resources.getString(cycleway.getTitleResId())

            if (isRight) {
                puzzleView.replaceRightSideImageResource(iconResId)
                puzzleView.setRightSideText(titleResId)
                rightSide = cycleway
            } else {
                puzzleView.replaceLeftSideImageResource(iconResId)
                puzzleView.setLeftSideText(titleResId)
                leftSide = cycleway
            }
            checkIsFormComplete()
        }.show()
    }

    private fun getCyclewayItems(isRight: Boolean): List<Cycleway> {
        val country = countryInfo.countryCode
        val values = DISPLAYED_CYCLEWAY_ITEMS.filter { it.isAvailableAsSelection(country) }.toMutableList()
        // different wording for a contraflow lane that is marked like a "shared" lane (just bicycle pictogram)
        if (isOneway && isReverseSideRight == isRight) {
            Collections.replaceAll(values, Cycleway.PICTOGRAMS, Cycleway.NONE_NO_ONEWAY)
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
        private const val IS_DISPLAYING_PREVIOUS_CYCLEWAY = "is_displaying_previous_cycleway"

    }
}
