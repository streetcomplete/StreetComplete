package de.westnordost.streetcomplete.quests.cycleway

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.getSelectableCyclewaysInCountry
import de.westnordost.streetcomplete.osm.cycleway.isAmbiguous
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isNotOnewayForCyclists
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.BOTH
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.LEFT
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.RIGHT
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class AddCyclewayForm : AStreetSideSelectForm<Cycleway, CyclewayAnswer>() {

    override val buttonPanelAnswers get() =
        if (isDisplayingPrevious) listOf(
            AnswerItem(R.string.quest_generic_hasFeature_no) { isDisplayingPrevious = false },
            AnswerItem(R.string.quest_generic_hasFeature_yes) { onClickOk() }
        )
        else emptyList()

    override val otherAnswers: List<AnswerItem> get() {
        val isNoRoundabout = element.tags["junction"] != "roundabout" && element.tags["junction"] != "circular"
        val result = mutableListOf<AnswerItem>()
        if (streetSideSelect.showSides != BOTH && isNoRoundabout) {
            result.add(AnswerItem(R.string.quest_cycleway_answer_contraflow_cycleway) { streetSideSelect.showSides = BOTH })
        }
        result.add(AnswerItem(R.string.quest_cycleway_answer_no_bicycle_infrastructure) { noCyclewayHereHint() })
        return result
    }

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
            or junction ~ roundabout|circular
        )
    """.toElementFilterExpression()

    /** returns whether the side that goes into the opposite direction of the driving direction of a
     * one-way is on the right side of the way */
    private val isReverseSideRight get() = isReversedOneway xor isLeftHandTraffic

    private val isOneway get() = isOneway(element.tags)

    private val isForwardOneway get() = isForwardOneway(element.tags)
    private val isReversedOneway get() = isReversedOneway(element.tags)

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    private fun isContraflowInOneway(isRight: Boolean): Boolean =
        isOneway && (isReverseSideRight xor !isRight)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        val sides = createCyclewaySides(element.tags, isLeftHandTraffic)
        val left = sides?.left?.takeIf { !it.isAmbiguous(countryInfo) && !it.isInvalid && !it.isUnknown }
        val right = sides?.right?.takeIf { !it.isAmbiguous(countryInfo) && !it.isInvalid && !it.isUnknown }
        val bothSidesWereDefinedBefore = sides?.left != null && sides.right != null
        val bicycleTrafficOnBothSidesIsLikely = !likelyNoBicycleContraflow.matches(element)

        streetSideSelect.showSides = when {
            bothSidesWereDefinedBefore || bicycleTrafficOnBothSidesIsLikely -> BOTH
            isLeftHandTraffic -> LEFT
            else -> RIGHT
        }
        val leftItem = left?.asStreetSideItem(countryInfo, isContraflowInOneway(false))
        streetSideSelect.setPuzzleSide(leftItem, false)

        val rightItem = right?.asStreetSideItem(countryInfo, isContraflowInOneway(true))
        streetSideSelect.setPuzzleSide(rightItem, true)

        // only show as re-survey (yes/no button) if the previous tagging was complete
        isDisplayingPrevious = streetSideSelect.isComplete
    }

    /* ---------------------------------- selection dialog -------------------------------------- */

    override fun onClickSide(isRight: Boolean) {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        val dialogItems = getSelectableCycleways(isRight)
            .map { it.asDialogItem(requireContext(), countryInfo, isContraflowInOneway) }

        ImageListPickerDialog(requireContext(), dialogItems, R.layout.labeled_icon_button_cell, 2) { item ->
            val streetSideItem = item.value!!.asStreetSideItem(countryInfo, isContraflowInOneway)
            streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
        }.show()
    }

    private fun getSelectableCycleways(isRight: Boolean): List<Cycleway> {
        val values = getSelectableCyclewaysInCountry(countryInfo).toMutableList()
        // different wording for a contraflow lane that is marked like a "shared" lane (just bicycle pictogram)
        if (isOneway && isReverseSideRight == isRight) {
            values.remove(Cycleway.PICTOGRAMS)
            values.add(values.indexOf(Cycleway.NONE) + 1, Cycleway.NONE_NO_ONEWAY)
        }
        return values
    }

    override fun serialize(item: Cycleway) =  item.name
    override fun deserialize(str: String) = Cycleway.valueOf(str)
    override fun asStreetSideItem(item: Cycleway, isRight: Boolean): StreetSideDisplayItem<Cycleway> {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        // NONE_NO_ONEWAY is displayed as simply NONE if not in contraflow because the former makes
        // only really sense in contraflow. This can only happen when applying the side(s) via the
        // last answer button
        val item2 = if (item == Cycleway.NONE_NO_ONEWAY && !isContraflowInOneway) Cycleway.NONE else item
        return item2.asStreetSideItem(countryInfo, isContraflowInOneway)
    }

    /* --------------------------------------- apply answer ------------------------------------- */

    override fun onClickOk() {
        val leftSide = streetSideSelect.left?.value
        val rightSide = streetSideSelect.right?.value

        // a cycleway that goes into opposite direction of a oneway street needs special tagging
        // as oneway:bicycle=* tag will differ from oneway=*
        // there is no need to tag cases where oneway:bicycle=* would merely repeat oneway=*
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
                || (if (isReverseSideRight) rightSide else leftSide) !== Cycleway.NONE
        }

        val answer = CyclewayAnswer(
            left = leftSide?.let { CyclewaySide(it, leftSideDir) },
            right = rightSide?.let { CyclewaySide(it, rightSideDir) },
            isOnewayNotForCyclists = isOnewayNotForCyclists
        )

        val wasOnewayNotForCyclists = isOneway && isNotOnewayForCyclists(element.tags, isLeftHandTraffic)
        if (!isOnewayNotForCyclists && wasOnewayNotForCyclists) {
            confirmNotOnewayForCyclists {
                applyAnswer(answer)
                streetSideSelect.saveLastSelection()
            }
        } else {
            applyAnswer(answer)
            streetSideSelect.saveLastSelection()
        }
    }

    private fun confirmNotOnewayForCyclists(callback: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_cycleway_confirmation_oneway_for_cyclists_too)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}

private fun Cycleway.isSingleTrackOrLane() =
    this === Cycleway.TRACK || this === Cycleway.EXCLUSIVE_LANE

private fun Cycleway.isDualTrackOrLane() =
    this === Cycleway.DUAL_TRACK || this === Cycleway.DUAL_LANE
