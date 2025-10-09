package de.westnordost.streetcomplete.quests.cycleway

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.LeftAndRightCycleway
import de.westnordost.streetcomplete.osm.cycleway.getDialogIcon
import de.westnordost.streetcomplete.osm.cycleway.getFloatingIcon
import de.westnordost.streetcomplete.osm.cycleway.getIcon
import de.westnordost.streetcomplete.osm.cycleway.getTitle
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.quests.AStreetSideSelectForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.IAnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.street_side_select.StreetSideItem
import de.westnordost.streetcomplete.util.ktx.noEntrySignDrawable
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.BOTH
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.LEFT
import de.westnordost.streetcomplete.view.controller.StreetSideSelectWithLastAnswerButtonViewController.Sides.RIGHT
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddCyclewayForm : AStreetSideSelectForm<CyclewayAndDirection, LeftAndRightCycleway>() {

    override val buttonPanelAnswers get() =
        if (isDisplayingPrevious) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) { isDisplayingPrevious = false },
                AnswerItem(R.string.quest_generic_hasFeature_yes) { onClickOk() }
            )
        } else {
            emptyList()
        }

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
        val direction = streetSideSelect.getPuzzleSide(isRight)?.direction
            ?: Direction.getDefault(isRight, isLeftHandTraffic)
        return isInContraflowOfOneway(element.tags, direction)
    }

    @Composable override fun BoxScope.DialogItemContent(item: CyclewayAndDirection, isRight: Boolean) {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        val icon = item.getDialogIcon(isRight, countryInfo, isContraflowInOneway)
        val title = item.getTitle(isContraflowInOneway)
        if (icon != null && title != null) {
            ImageWithLabel(
                painter = painterResource(icon),
                label = stringResource(title),
                imageRotation = if (countryInfo.isLeftHandTraffic) 180f else 0f
            )
        }
    }

    @Composable override fun getStreetSideItem(item: CyclewayAndDirection, isRight: Boolean): StreetSideItem? {
        val isContraflowInOneway = isContraflowInOneway(isRight)
        return StreetSideItem(
            image = item.getIcon(isRight, countryInfo, isContraflowInOneway)?.let { painterResource(it) },
            title = item.getTitle(isContraflowInOneway)?.let { stringResource(it) },
            floatingIcon = item.cycleway.getFloatingIcon(isContraflowInOneway, countryInfo.noEntrySignDrawable)?.let { painterResource(it) }
        )
    }

    /* ---------------------------------------- lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            initStateFromTags()
        }

        streetSideSelect.transformLastSelection = { item: CyclewayAndDirection, isRight: Boolean ->
            CyclewayAndDirection(item.cycleway, Direction.getDefault(isRight, isLeftHandTraffic))
        }
    }

    private fun initStateFromTags() {
        val cycleways = parseCyclewaySides(element.tags, isLeftHandTraffic)

        streetSideSelect.showSides = getInitiallyShownSides(cycleways)

        val onlyValidCycleways = cycleways?.selectableOrNullValues(countryInfo)
        val leftItem = onlyValidCycleways?.left?.asStreetSideItem(false, isContraflowInOneway(false), countryInfo)
        streetSideSelect.setPuzzleSide(leftItem, false)

        val rightItem = onlyValidCycleways?.right?.asStreetSideItem(true, isContraflowInOneway(true), countryInfo)
        streetSideSelect.setPuzzleSide(rightItem, true)

        // only show as re-survey (yes/no button) if the previous tagging was complete
        isDisplayingPrevious = streetSideSelect.isComplete
    }

    /* --------------------------------- showing only one side ---------------------------------- */

    private fun createShowBothSidesAnswer(): IAnswerItem? {
        val isNoRoundabout = element.tags["junction"] != "roundabout" && element.tags["junction"] != "circular"
        return if (streetSideSelect.showSides != BOTH && isNoRoundabout) {
            AnswerItem(R.string.quest_cycleway_answer_contraflow_cycleway) { streetSideSelect.showSides = BOTH }
        } else {
            null
        }
    }

    private fun getInitiallyShownSides(cycleways: LeftAndRightCycleway?): StreetSideSelectWithLastAnswerButtonViewController.Sides {
        val contraflowSide = if (isLeftHandTraffic) cycleways?.right else cycleways?.left
        val contraflowSideWasDefinedBefore = contraflowSide != null
        val bicycleTrafficOnBothSidesIsLikely = !likelyNoBicycleContraflow.matches(element)

        return when {
            contraflowSideWasDefinedBefore || bicycleTrafficOnBothSidesIsLikely -> BOTH
            isLeftHandTraffic -> LEFT
            else -> RIGHT
        }
    }

    private val likelyNoBicycleContraflow = """
        ways with oneway:bicycle != no and (
            oneway ~ yes|-1 and highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
            or dual_carriageway = yes
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
        val dialogItems = getSelectableCycleways(countryInfo, element.tags, isRight, isLeftHandTraffic, direction)
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
}
