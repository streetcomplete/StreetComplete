package de.westnordost.streetcomplete.quests.cycleway

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.asDialogItem
import de.westnordost.streetcomplete.osm.cycleway.asStreetSideItem
import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.getSelectableCycleways
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.BOTH
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.LEFT
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.RIGHT
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog

class AddCyclewayForm : AStreetSideSelectForm<Cycleway, LeftAndRightCycleway>() {

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
    private val isReversedOneway get() = isReversedOneway(element.tags)

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    private fun isContraflowInOneway(isRight: Boolean): Boolean =
        isOneway && (isReverseSideRight == isRight)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        val sides = createCyclewaySides(element.tags, isLeftHandTraffic)?.selectableOrNullValues(countryInfo)
        val bothSidesWereDefinedBefore = sides?.left != null && sides.right != null
        val bicycleTrafficOnBothSidesIsLikely = !likelyNoBicycleContraflow.matches(element)

        streetSideSelect.showSides = when {
            bothSidesWereDefinedBefore || bicycleTrafficOnBothSidesIsLikely -> BOTH
            isLeftHandTraffic -> LEFT
            else -> RIGHT
        }
        val leftItem = sides?.left?.asStreetSideItem(countryInfo, isContraflowInOneway(false))
        streetSideSelect.setPuzzleSide(leftItem, false)

        val rightItem = sides?.right?.asStreetSideItem(countryInfo, isContraflowInOneway(true))
        streetSideSelect.setPuzzleSide(rightItem, true)

        // only show as re-survey (yes/no button) if the previous tagging was complete
        isDisplayingPrevious = streetSideSelect.isComplete
    }

    /* ---------------------------------- selection dialog -------------------------------------- */

    override fun onClickSide(isRight: Boolean) {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        val dialogItems = getSelectableCycleways(countryInfo, element.tags, isRight)
            .map { it.asDialogItem(requireContext(), countryInfo, isContraflowInOneway) }

        ImageListPickerDialog(requireContext(), dialogItems, R.layout.labeled_icon_button_cell, 2) { item ->
            val streetSideItem = item.value!!.asStreetSideItem(countryInfo, isContraflowInOneway)
            streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
        }.show()
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
        val answer = LeftAndRightCycleway(streetSideSelect.left?.value, streetSideSelect.right?.value)
        if (answer.wasNoOnewayForCyclistsButNowItIs(element.tags, isLeftHandTraffic)) {
            confirmNotOnewayForCyclists {
                streetSideSelect.saveLastSelection()
                applyAnswer(answer)
            }
        } else {
            streetSideSelect.saveLastSelection()
            applyAnswer(answer)
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
