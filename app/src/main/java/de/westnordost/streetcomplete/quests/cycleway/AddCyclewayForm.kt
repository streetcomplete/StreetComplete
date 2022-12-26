package de.westnordost.streetcomplete.quests.cycleway

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.Direction
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.asDialogItem
import de.westnordost.streetcomplete.osm.cycleway.asStreetSideItem
import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.getDefault
import de.westnordost.streetcomplete.osm.cycleway.getSelectableCycleways
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.isInContraflowOfOneway
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.IAnswerItem
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.BOTH
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.LEFT
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.RIGHT
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AddCyclewayForm : AStreetSideSelectForm<CyclewayAndDirection, LeftAndRightCycleway>() {

    override val buttonPanelAnswers get() =
        if (isDisplayingPrevious) listOf(
            AnswerItem(R.string.quest_generic_hasFeature_no) { isDisplayingPrevious = false },
            AnswerItem(R.string.quest_generic_hasFeature_yes) { onClickOk() }
        )
        else emptyList()

    override val otherAnswers: List<IAnswerItem> get() = listOfNotNull(
        createShowBothSidesAnswer(),
        AnswerItem(R.string.quest_cycleway_answer_no_bicycle_infrastructure, ::noCyclewayHereHint),
        AnswerItem(R.string.cycleway_reverse_direction, ::selectReverseCyclewayDirection)
    )

    private fun noCyclewayHereHint() {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_cycleway_answer_no_bicycle_infrastructure_title)
            .setMessage(R.string.quest_cycleway_answer_no_bicycle_infrastructure_explanation)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    private var reverseDirection: Boolean = false

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    private fun isContraflowInOneway(isRight: Boolean): Boolean {
        val direction = streetSideSelect.getPuzzleSide(isRight)?.value?.direction
            ?: Direction.getDefault(isRight, isLeftHandTraffic)
        return isInContraflowOfOneway(element.tags, direction)
    }

    /* ---------------------------------------- lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initStateFromTags()
        }
    }

    private fun initStateFromTags() {
        val cycleways = createCyclewaySides(element.tags, isLeftHandTraffic)?.selectableOrNullValues(countryInfo)

        streetSideSelect.showSides = getInitiallyShownSides(cycleways)

        val leftItem = cycleways?.left?.asStreetSideItem(false, isContraflowInOneway(false), countryInfo)
        streetSideSelect.setPuzzleSide(leftItem, false)

        val rightItem = cycleways?.right?.asStreetSideItem(true, isContraflowInOneway(true), countryInfo)
        streetSideSelect.setPuzzleSide(rightItem, true)

        // only show as re-survey (yes/no button) if the previous tagging was complete
        isDisplayingPrevious = streetSideSelect.isComplete
    }

    /* --------------------------------- showing only one side ---------------------------------- */

    private fun createShowBothSidesAnswer(): IAnswerItem? {
        val isNoRoundabout = element.tags["junction"] != "roundabout" && element.tags["junction"] != "circular"
        return if (streetSideSelect.showSides != BOTH && isNoRoundabout) {
            AnswerItem(R.string.quest_cycleway_answer_contraflow_cycleway) { streetSideSelect.showSides = BOTH }
        } else null
    }

    private fun getInitiallyShownSides(cycleways: LeftAndRightCycleway?): StreetSideSelectWithLastAnswerButtonViewController.Sides {
        val bothSidesWereDefinedBefore = cycleways?.left != null && cycleways.right != null
        val bicycleTrafficOnBothSidesIsLikely = !likelyNoBicycleContraflow.matches(element)

        return when {
            bothSidesWereDefinedBefore || bicycleTrafficOnBothSidesIsLikely -> BOTH
            isLeftHandTraffic -> LEFT
            else -> RIGHT
        }
    }

    private val likelyNoBicycleContraflow = """
        ways with oneway:bicycle != no and (
            oneway ~ yes|-1 and highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
            or junction ~ roundabout|circular
        )
    """.toElementFilterExpression()

    /* ------------------------------ reverse cycleway direction -------------------------------- */

    private fun selectReverseCyclewayDirection() {
        confirmSelectReverseCyclewayDirection {
            reverseDirection = true
            context?.toast(R.string.cycleway_reverse_direction_toast)
        }
    }

    private fun confirmSelectReverseCyclewayDirection(callback: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.cycleway_reverse_direction_warning)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    private fun reverseCyclewayDirection(isRight: Boolean) {
        reverseDirection = false
        val value = streetSideSelect.getPuzzleSide(isRight)?.value ?: return
        val newValue = value.copy(direction = value.direction.reverse())
        val newItem = newValue.asStreetSideItem(isRight, isContraflowInOneway(isRight), countryInfo)
        streetSideSelect.replacePuzzleSide(newItem, isRight)
    }

    /* --------------------------------- select & apply answer ---------------------------------- */

    override fun onClickSide(isRight: Boolean) {
        if (reverseDirection) {
            reverseCyclewayDirection(isRight)
        } else {
            selectCycleway(isRight)
        }
    }

    private fun selectCycleway(isRight: Boolean) {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        val direction = streetSideSelect.getPuzzleSide(isRight)?.value?.direction
            ?: Direction.getDefault(isRight, isLeftHandTraffic)
        val dialogItems = getSelectableCycleways(countryInfo, element.tags, direction)
            .map { it.asDialogItem(isRight, isContraflowInOneway, requireContext(), countryInfo) }

        ImageListPickerDialog(requireContext(), dialogItems, R.layout.labeled_icon_button_cell, 2) { item ->
            val streetSideItem = item.value!!.asStreetSideItem(isRight, isContraflowInOneway, countryInfo)
            streetSideSelect.replacePuzzleSide(streetSideItem, isRight)
        }.show()
    }

    override fun onClickOk() {
        val cycleways = LeftAndRightCycleway(streetSideSelect.left?.value, streetSideSelect.right?.value)
        if (cycleways.wasNoOnewayForCyclistsButNowItIs(element.tags, isLeftHandTraffic)) {
            confirmNotOnewayForCyclists { saveAndApplyCycleway(cycleways) }
        } else {
            saveAndApplyCycleway(cycleways)
        }
    }

    private fun confirmNotOnewayForCyclists(callback: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.quest_cycleway_confirmation_oneway_for_cyclists_too)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    private fun saveAndApplyCycleway(cycleways: LeftAndRightCycleway) {
        streetSideSelect.saveLastSelection()
        applyAnswer(cycleways)
    }

    /* --------------------------------- AStreetSideSelectForm ---------------------------------- */

    override fun serialize(item: CyclewayAndDirection) = Json.encodeToString(item)
    override fun deserialize(str: String) = Json.decodeFromString<CyclewayAndDirection>(str)
    override fun asStreetSideItem(item: CyclewayAndDirection, isRight: Boolean): StreetSideDisplayItem<CyclewayAndDirection> {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        // NONE_NO_ONEWAY is displayed as simply NONE if not in contraflow because the former makes
        // only really sense in contraflow. This can only happen when applying the side(s) via the
        // last answer button
        val item2 = if (item.cycleway == Cycleway.NONE_NO_ONEWAY && !isContraflowInOneway) item.copy(cycleway = Cycleway.NONE) else item
        return item2.asStreetSideItem(isRight, isContraflowInOneway, countryInfo)
    }
}
