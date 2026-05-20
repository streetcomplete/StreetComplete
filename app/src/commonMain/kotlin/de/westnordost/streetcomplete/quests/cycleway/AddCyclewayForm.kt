package de.westnordost.streetcomplete.quests.cycleway

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.all
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.CyclewayForm
import de.westnordost.streetcomplete.osm.cycleway.CyclewayFormSelectionMode
import de.westnordost.streetcomplete.osm.cycleway.isSelectable
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.cycleway.withDefaultDirection
import de.westnordost.streetcomplete.osm.oneway.Direction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddCyclewayForm(
    onAnswer: (Sides<CyclewayAndDirection>) -> Unit,
    element: Element,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    preferences: Preferences = koinInject(),
)  {
    val favKey = "AddCyclewayForm"
    val context = LocalContext.current

    val originalCycleway = remember {
        parseCyclewaySides(element.tags, countryInfo.isLeftHandTraffic)
            ?.selectableOrNullValues(countryInfo)
            ?: Sides<CyclewayAndDirection>(null, null)
    }

    val likelyNoBicycleContraflow = remember { """
        ways with oneway:bicycle != no and (
            oneway ~ yes|-1 and highway ~ primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified
            or dual_carriageway = yes
            or junction ~ roundabout|circular
        )
    """.toElementFilterExpression()
    }

    val showBothSides = remember {
        val contraflowSide =
            if (countryInfo.isLeftHandTraffic) originalCycleway.right
            else originalCycleway.left
        val contraflowSideWasDefinedBefore = contraflowSide != null
        val bicycleTrafficOnBothSidesIsLikely = !likelyNoBicycleContraflow.matches(element)
        contraflowSideWasDefinedBefore || bicycleTrafficOnBothSidesIsLikely
    }

    val lastPicked = remember {
        if (showBothSides) {
            preferences.getLastPicked<Sides<Cycleway>>(favKey)
                .takeFavorites(n = 5, history = 15, first = 1)
                .map { it.withDefaultDirection(countryInfo.isLeftHandTraffic) }
                .filter { sides -> sides.all { it?.isSelectable(countryInfo) != false } }
        } else {
            emptyList()
        }
    }

    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }

    val isRoundabout = remember {
        element.tags["junction"] == "roundabout" || element.tags["junction"] == "circular"
    }

    var isDisplayingPrevious by rememberSaveable {
        // only show as re-survey (yes/no button) if the previous tagging was complete
        mutableStateOf(originalCycleway.all { it != null })
    }
    var cycleways by rememberSerializable { mutableStateOf(originalCycleway) }
    var isLeftSideVisible by rememberSerializable {
        mutableStateOf(showBothSides || countryInfo.isLeftHandTraffic)
    }
    var isRightSideVisible by rememberSerializable {
        mutableStateOf(showBothSides || !countryInfo.isLeftHandTraffic)
    }
    var selectionMode by rememberSerializable { mutableStateOf(CyclewayFormSelectionMode.SELECT) }

    var showNoCyclewayHereHint by remember { mutableStateOf(false) }
    var confirmNotOnewayForCyclists by remember { mutableStateOf(false) }
    var confirmSelectReverseCyclewayDirection by remember { mutableStateOf(false) }

    fun saveAndApplyCycleway(sides: Sides<CyclewayAndDirection>) {
        if (sides.left != null && sides.right != null) {
            // only persist the cycleway selection, not the direction. For any road that deviates from
            // the default, the user should select this specifically. Simply carrying over the
            // non-default direction to the next answer might result in mistakes
            val cycleways = Sides(left = sides.left.cycleway, right = sides.right.cycleway)
            preferences.setLastPicked(favKey, listOf(cycleways))
        }
        onAnswer(sides)
    }

    val content: @Composable () -> Unit = {
        CyclewayForm(
            value = cycleways,
            onValueChanged = {
                cycleways = it
                selectionMode = CyclewayFormSelectionMode.SELECT
            },
            selectionMode = selectionMode,
            geometryRotation = geometryRotation,
            mapRotation = LocalMapRotation.current,
            mapTilt = LocalMapTilt.current,
            countryInfo = countryInfo,
            roadDirection = Direction.from(element.tags),
            lastPicked = lastPicked,
            enabled = !isDisplayingPrevious,
            isLeftSideVisible = isLeftSideVisible,
            isRightSideVisible = isRightSideVisible,
        )
    }

    if (isDisplayingPrevious) {
        QuestForm(
            title = stringResource(Res.string.quest_cycleway_resurvey_title),
            answers = listOf(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { isDisplayingPrevious = false },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(cycleways) }
            ),
            contentPadding = PaddingValues.Zero,
            content = { content() }
        )
    } else {
        QuestForm(
            isComplete =
                (cycleways.left != null || !isLeftSideVisible) &&
                (cycleways.right != null || !isRightSideVisible),
            hasChanges =
                cycleways.left != null || cycleways.right != null,
            onClickOk = {
                if (cycleways.wasNoOnewayForCyclistsButNowItIs(element.tags, countryInfo.isLeftHandTraffic)) {
                    confirmNotOnewayForCyclists = true
                } else {
                    saveAndApplyCycleway(cycleways)
                }
            },
            otherAnswers = listOfNotNull(
                if (isLeftSideVisible && isRightSideVisible || isRoundabout) {
                    null
                } else {
                    Answer(stringResource(Res.string.quest_cycleway_answer_contraflow_cycleway)) {
                        isLeftSideVisible = true
                        isRightSideVisible = true
                    }
                },
                Answer(stringResource(Res.string.quest_cycleway_answer_no_bicycle_infrastructure)) {
                    showNoCyclewayHereHint = true
                },
                Answer(stringResource(Res.string.cycleway_reverse_direction)) {
                    confirmSelectReverseCyclewayDirection = true
                }
            ),
            contentPadding = PaddingValues.Zero,
            content = { content() }
        )
    }

    if (showNoCyclewayHereHint) {
        InfoDialog(
            onDismissRequest = { showNoCyclewayHereHint = false },
            title = { Text(stringResource(Res.string.quest_cycleway_answer_no_bicycle_infrastructure_title)) },
            text = { Text(stringResource(Res.string.quest_cycleway_answer_no_bicycle_infrastructure_explanation)) }
        )
    }

    if (confirmNotOnewayForCyclists) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmNotOnewayForCyclists = false },
            onConfirmed = { saveAndApplyCycleway(cycleways) },
            titleText = null,
            text = { Text(stringResource(Res.string.quest_cycleway_confirmation_oneway_for_cyclists_too)) }
        )
    }

    if (confirmSelectReverseCyclewayDirection) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmSelectReverseCyclewayDirection = false },
            onConfirmed = {
                selectionMode = CyclewayFormSelectionMode.REVERSE
                context.toast(R.string.cycleway_reverse_direction_toast)
            },
            text = { Text(stringResource(Res.string.cycleway_reverse_direction_warning)) }
        )
    }
}
