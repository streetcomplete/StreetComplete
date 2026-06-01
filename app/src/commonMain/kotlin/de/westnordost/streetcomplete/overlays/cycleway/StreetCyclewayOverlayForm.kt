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
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.all
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_boulevard.applyTo
import de.westnordost.streetcomplete.osm.bicycle_boulevard.parseBicycleBoulevard
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.BicycleBoulevardSign
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.BicycleInPedestrianStreet
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.BicycleInPedestrianStreetAllowedSign
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.BicycleInPedestrianStreetDesignatedSign
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.applyTo
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.parseBicycleInPedestrianStreet
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.CyclewayAndDirection
import de.westnordost.streetcomplete.osm.cycleway.CyclewayForm
import de.westnordost.streetcomplete.osm.cycleway.CyclewayFormSelectionMode
import de.westnordost.streetcomplete.osm.cycleway.applyTo
import de.westnordost.streetcomplete.osm.cycleway.isSelectable
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.selectableOrNullValues
import de.westnordost.streetcomplete.osm.cycleway.wasNoOnewayForCyclistsButNowItIs
import de.westnordost.streetcomplete.osm.cycleway.withDefaultDirection
import de.westnordost.streetcomplete.osm.oneway.Direction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.overlay.OverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun StreetCyclewayOverlayForm(
    on: (OverlayAction) -> Unit,
    element: Element,
    geometry: ElementGeometry,
    countryInfo: CountryInfo,
    preferences: Preferences = koinInject()
) {
    val favKey = "StreetCyclewayOverlayForm"
    val lastPicked = remember {
        preferences
            .getLastPicked<Sides<Cycleway>>(favKey)
            .map { it.withDefaultDirection(countryInfo.isLeftHandTraffic) }
            .filter { sides -> sides.all { it?.isSelectable(countryInfo) != false } }
    }
    val originalCycleway = remember(element) {
        parseCyclewaySides(element.tags, countryInfo.isLeftHandTraffic)
            ?.selectableOrNullValues(countryInfo)
            ?: Sides<CyclewayAndDirection>(null, null)
    }
    val originalBicycleBoulevard = remember(element) { parseBicycleBoulevard(element.tags) }
    val originalBicycleInPedestrianStreet = remember(element) { parseBicycleInPedestrianStreet(element.tags) }

    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }

    var cycleways by rememberSerializable(originalCycleway) {
        mutableStateOf(originalCycleway)
    }
    var bicycleBoulevard by rememberSerializable(originalBicycleBoulevard) {
        mutableStateOf(originalBicycleBoulevard)
    }
    var bicycleInPedestrianStreet by rememberSerializable(originalBicycleInPedestrianStreet) {
        mutableStateOf(originalBicycleInPedestrianStreet)
    }
    var selectionMode by remember { mutableStateOf(CyclewayFormSelectionMode.SELECT)  }

    var confirmNotOnewayForCyclists by remember { mutableStateOf(false) }
    var confirmSelectReverseCyclewayDirection by remember { mutableStateOf(false) }

    fun saveAndApplyCycleway() {
        val tags = StringMapChangesBuilder(element.tags)
        val sides = cycleways

        if (sides.left != null && sides.right != null) {
            // only persist the cycleway selection, not the direction. For any road that deviates from
            // the default, the user should select this specifically. Simply carrying over the
            // non-default direction to the next answer might result in mistakes
            val cycleways = Sides(left = sides.left.cycleway, right = sides.right.cycleway)
            preferences.setLastPicked("StreetCyclewayOverlayForm", listOf(cycleways))
        }

        sides.applyTo(tags, countryInfo.isLeftHandTraffic)
        bicycleBoulevard.applyTo(tags, countryInfo.countryCode)
        bicycleInPedestrianStreet?.applyTo(tags)

        on(Edit(UpdateElementTagsAction(element, tags.create())))
    }

    @Composable
    fun createOtherAnswers(): List<AnswerItem> {
        val result = ArrayList<AnswerItem>()

        if (bicycleInPedestrianStreet != null) {
            if (bicycleInPedestrianStreet != BicycleInPedestrianStreet.DESIGNATED) {
                result.add(
                    AnswerItem(stringResource(Res.string.pedestrian_zone_designated)) {
                        bicycleInPedestrianStreet = BicycleInPedestrianStreet.DESIGNATED
                    }
                )
            }
            if (bicycleInPedestrianStreet != BicycleInPedestrianStreet.ALLOWED) {
                result.add(
                    AnswerItem(stringResource(Res.string.pedestrian_zone_allowed_sign)) {
                        bicycleInPedestrianStreet = BicycleInPedestrianStreet.ALLOWED
                    }
                )
            }
            if (bicycleInPedestrianStreet != BicycleInPedestrianStreet.NOT_SIGNED) {
                result.add(
                    AnswerItem(stringResource(Res.string.pedestrian_zone_no_sign)) {
                        bicycleInPedestrianStreet = BicycleInPedestrianStreet.NOT_SIGNED
                    }
                )
            }
        }

        result.add(AnswerItem(stringResource(Res.string.cycleway_reverse_direction)) {
            confirmSelectReverseCyclewayDirection = true
        })

        when (bicycleBoulevard) {
            BicycleBoulevard.YES ->
                result.add(AnswerItem(stringResource(Res.string.bicycle_boulevard_is_not_a2)) {
                    bicycleBoulevard = BicycleBoulevard.NO
                })
            BicycleBoulevard.NO ->
                // don't allow pedestrian roads to be tagged as bicycle roads (should rather be
                // highway=pedestrian + bicycle=designated rather than bicycle_road=yes)
                if (element.tags["highway"] != "pedestrian") {
                    result.add(AnswerItem(stringResource(Res.string.bicycle_boulevard_is_a2)) {
                        bicycleBoulevard = BicycleBoulevard.YES
                    })
                }
        }

        return result
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
            if (cycleways.wasNoOnewayForCyclistsButNowItIs(element.tags, countryInfo.isLeftHandTraffic)) {
                confirmNotOnewayForCyclists = true
            } else {
                saveAndApplyCycleway()
            }
        },
        on = on,
        otherAnswers = ::createOtherAnswers,
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
                geometryRotation = geometryRotation,
                mapRotation = LocalMapRotation.current,
                mapTilt = LocalMapTilt.current,
                countryInfo = countryInfo,
                roadDirection = Direction.from(element.tags),
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
            onConfirmed = { saveAndApplyCycleway() },
            text = { Text(stringResource(Res.string.quest_cycleway_confirmation_oneway_for_cyclists_too)) }
        )
    }

    if (confirmSelectReverseCyclewayDirection) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmSelectReverseCyclewayDirection = false },
            onConfirmed = { selectionMode = CyclewayFormSelectionMode.REVERSE },
            titleText = stringResource(Res.string.quest_generic_confirmation_title),
            text = { Text(stringResource(Res.string.cycleway_reverse_direction_warning)) }
        )
    }
}

