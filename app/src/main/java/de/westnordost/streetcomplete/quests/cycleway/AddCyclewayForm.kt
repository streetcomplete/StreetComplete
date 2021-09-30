package de.westnordost.streetcomplete.quests.cycleway

import android.os.Bundle
import androidx.annotation.AnyThread
import android.view.View
import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.databinding.QuestStreetSidePuzzleBinding
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class AddCyclewayForm : AbstractQuestFormAnswerFragment<CyclewayAnswer>() {

    override val contentLayoutResId = R.layout.quest_street_side_puzzle
    private val binding by contentViewBinding(QuestStreetSidePuzzleBinding::bind)

    override val buttonPanelAnswers get() =
        if(isDisplayingPreviousCycleway) listOf(
            AnswerItem(R.string.quest_generic_hasFeature_no) { setAsResurvey(false) },
            AnswerItem(R.string.quest_generic_hasFeature_yes) { onClickOk() }
        )
        else emptyList()

    override val otherAnswers: List<AnswerItem> get() {
        val isNoRoundabout = osmElement!!.tags["junction"] != "roundabout"
        val result = mutableListOf<AnswerItem>()
        if (!isDefiningBothSides && isNoRoundabout) {
            result.add(AnswerItem(R.string.quest_cycleway_answer_contraflow_cycleway) { showBothSides() })
        }
        result.add(AnswerItem(R.string.quest_cycleway_answer_no_bicycle_infrastructure) { noCyclewayHereHint() })
        return result
    }


    override val contentPadding = false

    private var isDisplayingPreviousCycleway: Boolean = false

    private fun noCyclewayHereHint() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_cycleway_answer_no_bicycle_infrastructure_title)
            .setMessage(R.string.quest_cycleway_answer_no_bicycle_infrastructure_explanation)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }


    private val likelyNoBicycleContraflow = """
            ways with oneway:bicycle != no and (
                oneway ~ yes|-1 and highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
                or junction = roundabout
            )
        """.toElementFilterExpression()

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

        binding.puzzleView.onClickSideListener = { isRight -> showCyclewaySelectionDialog(isRight) }

        streetSideRotater = StreetSideRotater(
            binding.puzzleView,
            binding.littleCompass.root,
            elementGeometry as ElementPolylinesGeometry
        )

        if (!isDefiningBothSides) {
            if (isLeftHandTraffic) binding.puzzleView.showOnlyLeftSide()
            else                   binding.puzzleView.showOnlyRightSide()
        }

        val defaultResId =
            if (isLeftHandTraffic) R.drawable.ic_cycleway_unknown_l
            else                   R.drawable.ic_cycleway_unknown

        binding.puzzleView.setLeftSideImage(ResImage(leftSide?.getIconResId(isLeftHandTraffic) ?: defaultResId))
        binding.puzzleView.setRightSideImage(ResImage(rightSide?.getIconResId(isLeftHandTraffic) ?: defaultResId))
        binding.puzzleView.setLeftSideText(leftSide?.getTitleResId()?.let { resources.getString(it) })
        binding.puzzleView.setRightSideText(rightSide?.getTitleResId()?.let { resources.getString(it) })
        if ((leftSide == null || rightSide == null) && !HAS_SHOWN_TAP_HINT) {
            if (leftSide == null) binding.puzzleView.showLeftSideTapHint()
            if (rightSide == null) binding.puzzleView.showRightSideTapHint()
            HAS_SHOWN_TAP_HINT = true
        }

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
        setAsResurvey(savedInstanceState.getBoolean(IS_DISPLAYING_PREVIOUS_CYCLEWAY))

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
        binding.puzzleView.isEnabled = !resurvey
        updateButtonPanel()
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
                binding.puzzleView.replaceRightSideImage(ResImage(iconResId))
                binding.puzzleView.setRightSideText(titleResId)
                rightSide = cycleway
            } else {
                binding.puzzleView.replaceLeftSideImage(ResImage(iconResId))
                binding.puzzleView.setLeftSideText(titleResId)
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
            values.remove(Cycleway.PICTOGRAMS)
            values.add(values.indexOf(Cycleway.NONE) + 1, Cycleway.NONE_NO_ONEWAY)
        }
        return values
    }

    private fun showBothSides() {
        isDefiningBothSides = true
        binding.puzzleView.showBothSides()
        checkIsFormComplete()
    }

    companion object {
        private const val CYCLEWAY_LEFT = "cycleway_left"
        private const val CYCLEWAY_RIGHT = "cycleway_right"
        private const val DEFINE_BOTH_SIDES = "define_both_sides"
        private const val IS_DISPLAYING_PREVIOUS_CYCLEWAY = "is_displaying_previous_cycleway"

        private var HAS_SHOWN_TAP_HINT = false
    }
}
