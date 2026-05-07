package de.westnordost.streetcomplete.overlays.cycleway

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.all
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_boulevard.applyTo
import de.westnordost.streetcomplete.osm.bicycle_boulevard.parseBicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.BicycleInPedestrianStreet
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.applyTo
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.parseBicycleInPedestrianStreet
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.applyTo
import de.westnordost.streetcomplete.osm.cycleway.isSelectable
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.cycleway.withDefaultDirection
import de.westnordost.streetcomplete.osm.oneway.Direction
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.quests.cycleway.BicycleBoulevardSign
import de.westnordost.streetcomplete.quests.cycleway.BicycleInPedestrianStreetAllowedSign
import de.westnordost.streetcomplete.quests.cycleway.BicycleInPedestrianStreetDesignatedSign
import de.westnordost.streetcomplete.quests.cycleway.CyclewayForm
import de.westnordost.streetcomplete.quests.cycleway.CyclewayFormSelectionMode
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toast
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class StreetCyclewayOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val lastPicked = remember {
            prefs
                .getLastPicked<Sides<Cycleway>>("StreetCyclewayOverlayForm")
                .map { it.withDefaultDirection(countryInfo.isLeftHandTraffic) }
                .filter { sides -> sides.all { it?.isSelectable(countryInfo) != false } }
        }
        val tags = element!!.tags
        val originalCycleway = remember {
            parseCyclewaySides(tags, isLeftHandTraffic)
                ?.selectableOrNullValues(countryInfo)
                ?: Sides<CyclewayAndDirection>(null, null)
        }
        val originalBicycleBoulevard = remember { parseBicycleBoulevard(tags) }
        val originalBicycleInPedestrianStreet = remember { parseBicycleInPedestrianStreet(tags) }

        var cycleways by rememberSerializable { mutableStateOf(originalCycleway) }
        var bicycleBoulevard by rememberSerializable { mutableStateOf(originalBicycleBoulevard) }
        var bicycleInPedestrianStreet by rememberSerializable { mutableStateOf(originalBicycleInPedestrianStreet) }
        var selectionMode by remember { mutableStateOf(CyclewayFormSelectionMode.SELECT)  }

        var confirmNotOnewayForCyclists by remember { mutableStateOf(false) }
        var confirmSelectReverseCyclewayDirection by remember { mutableStateOf(false) }

        val switchBicycleBoulevardAnswer = when (bicycleBoulevard) {
            BicycleBoulevard.YES ->
                Answer(stringResource(Res.string.bicycle_boulevard_is_not_a, stringResource(Res.string.bicycle_boulevard))) {
                    bicycleBoulevard = BicycleBoulevard.NO
                }
            BicycleBoulevard.NO ->
                // don't allow pedestrian roads to be tagged as bicycle roads (should rather be
                // highway=pedestrian + bicycle=designated rather than bicycle_road=yes)
                if (tags["highway"] != "pedestrian") {
                    Answer(stringResource(Res.string.bicycle_boulevard_is_a, stringResource(Res.string.bicycle_boulevard))) {
                        bicycleBoulevard = BicycleBoulevard.YES
                    }
                } else {
                    null
                }
        }
        val reverseCyclewayDirectionAnswer = Answer(stringResource(Res.string.cycleway_reverse_direction)) {
            confirmSelectReverseCyclewayDirection = true
        }
        val bicycleInPedestrianStreetAnswers = buildList {
            // only offer answers in pedestrian zones
            if (bicycleInPedestrianStreet == null) return@buildList

            if (bicycleInPedestrianStreet != BicycleInPedestrianStreet.DESIGNATED) {
                add(
                    Answer(stringResource(Res.string.pedestrian_zone_designated)) {
                        bicycleInPedestrianStreet = BicycleInPedestrianStreet.DESIGNATED
                    }
                )
            }
            if (bicycleInPedestrianStreet != BicycleInPedestrianStreet.ALLOWED) {
                add(
                    Answer(stringResource(Res.string.pedestrian_zone_allowed_sign)) {
                        bicycleInPedestrianStreet = BicycleInPedestrianStreet.ALLOWED
                    }
                )
            }
            if (bicycleInPedestrianStreet != BicycleInPedestrianStreet.NOT_SIGNED) {
                add(
                    Answer(stringResource(Res.string.pedestrian_zone_no_sign)) {
                        bicycleInPedestrianStreet = BicycleInPedestrianStreet.NOT_SIGNED
                    }
                )
            }
        }

        OverlayForm(
            isComplete =
                cycleways.left != null ||
                cycleways.right != null ||
                originalBicycleBoulevard != bicycleBoulevard ||
                originalBicycleInPedestrianStreet != bicycleInPedestrianStreet,
            hasChanges =
                cycleways.left != originalCycleway.left ||
                cycleways.right != originalCycleway.right ||
                originalBicycleBoulevard != bicycleBoulevard ||
                originalBicycleInPedestrianStreet != bicycleInPedestrianStreet,
            onClickOk = {
                if (cycleways.wasNoOnewayForCyclistsButNowItIs(element!!.tags, isLeftHandTraffic)) {
                    confirmNotOnewayForCyclists = true
                } else {
                    saveAndApplyCycleway(cycleways, bicycleBoulevard, bicycleInPedestrianStreet)
                }
            },
            otherAnswers =
                bicycleInPedestrianStreetAnswers +
                listOfNotNull(
                    reverseCyclewayDirectionAnswer,
                    switchBicycleBoulevardAnswer,
                ),
            contentPadding = PaddingValues.Zero
        ) {
            Box(contentAlignment = Alignment.Center) {
                CyclewayForm(
                    value = cycleways,
                    onValueChanged = {
                        cycleways = it
                        selectionMode = CyclewayFormSelectionMode.SELECT
                    },
                    selectionMode = selectionMode,
                    geometryRotation = geometryRotation.floatValue,
                    mapRotation = mapRotation.floatValue,
                    mapTilt = mapTilt.floatValue,
                    countryInfo = countryInfo,
                    roadDirection = Direction.from(element!!.tags),
                    lastPicked = lastPicked,
                    lastPickedContentPadding = PaddingValues(start = 48.dp, end = 56.dp),
                )
                Box(Modifier.scale(0.5f).alpha(0.75f)) {
                    if (bicycleInPedestrianStreet == BicycleInPedestrianStreet.ALLOWED) {
                        BicycleInPedestrianStreetAllowedSign()
                    } else if (bicycleInPedestrianStreet == BicycleInPedestrianStreet.DESIGNATED) {
                        BicycleInPedestrianStreetDesignatedSign()
                    } else if (bicycleBoulevard == BicycleBoulevard.YES) {
                        BicycleBoulevardSign()
                    }
                }
            }
        }

        if (confirmNotOnewayForCyclists) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNotOnewayForCyclists = false },
                onConfirmed = { saveAndApplyCycleway(cycleways, bicycleBoulevard, bicycleInPedestrianStreet) },
                text = { Text(stringResource(Res.string.quest_cycleway_confirmation_oneway_for_cyclists_too)) }
            )
        }

        if (confirmSelectReverseCyclewayDirection) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmSelectReverseCyclewayDirection = false },
                onConfirmed = {
                    selectionMode = CyclewayFormSelectionMode.REVERSE
                    context?.toast(R.string.cycleway_reverse_direction_toast)
                },
                titleText = stringResource(Res.string.quest_generic_confirmation_title),
                text = { Text(stringResource(Res.string.cycleway_reverse_direction_warning)) }
            )
        }
    }

    // just a shortcut
    private val isLeftHandTraffic get() = countryInfo.isLeftHandTraffic

    private fun saveAndApplyCycleway(
        sides: Sides<CyclewayAndDirection>,
        bicycleBoulevard: BicycleBoulevard,
        bicycleInPedestrianStreet: BicycleInPedestrianStreet?
    ) {
        val tags = StringMapChangesBuilder(element!!.tags)
        sides.applyTo(tags, countryInfo.isLeftHandTraffic)
        bicycleBoulevard.applyTo(tags, countryInfo.countryCode)
        bicycleInPedestrianStreet?.applyTo(tags)
        if (sides.left != null && sides.right != null) {
            // only persist the cycleway selection, not the direction. For any road that deviates from
            // the default, the user should select this specifically. Simply carrying over the
            // non-default direction to the next answer might result in mistakes
            val cycleways = Sides(left = sides.left.cycleway, right = sides.right.cycleway)
            prefs.setLastPicked("StreetCyclewayOverlayForm", listOf(cycleways))
        }
        applyEdit(UpdateElementTagsAction(element!!, tags.create()))
    }
}
