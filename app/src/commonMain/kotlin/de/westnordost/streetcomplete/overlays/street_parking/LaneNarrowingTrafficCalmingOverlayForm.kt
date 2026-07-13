package de.westnordost.streetcomplete.overlays.street_parking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.traffic_calming.LaneNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.traffic_calming.applyTo
import de.westnordost.streetcomplete.osm.traffic_calming.icon
import de.westnordost.streetcomplete.osm.traffic_calming.parseNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.traffic_calming.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LaneNarrowingTrafficCalmingForm(
    on: (OverlayAction) -> Unit,
    element: Element?,
    position: LatLon?,
    onPinPosition: (icon: DrawableResource, position: LatLon?) -> Unit,
    metersPerPixel: Double,
    mapDataWithEditsSource: MapDataWithEditsSource = koinInject(),
    preferences: Preferences = koinInject()
) {
    val originalLaneNarrowingTrafficCalming = remember(element) {
        element?.tags?.let { parseNarrowingTrafficCalming(it) }
    }

    val roadLines = remember<Collection<Pair<Way, List<LatLon>>>?>(position != null) {
        position?.let {
            mapDataWithEditsSource.getRoadLines(position.enclosingBoundingBox(100.0))
        }
    }
    val maxDistanceToCrosshair = metersPerPixel * 24.dp.toPx()
    val snapToVertexDistance = metersPerPixel * 12.dp.toPx()

    val positionOnWay = remember(position, roadLines) {
        if (position == null) return@remember null
        if (roadLines == null) return@remember null

        position.getPositionOnWays(
            ways = roadLines,
            maxDistance = maxDistanceToCrosshair,
            snapToVertexDistance = snapToVertexDistance
        )
    }

    LaunchedEffect(positionOnWay) {
        onPinPosition(Res.drawable.quest_choker, positionOnWay?.position)
    }

    var confirmRemoveLaneNarrowingTrafficCalming by remember { mutableStateOf(false) }

    ItemSelectOverlayForm(
        on = on,
        isComplete = element != null || positionOnWay != null,
        itemsPerRow = 2,
        items = LaneNarrowingTrafficCalming.entries,
        initialSelectedItem = originalLaneNarrowingTrafficCalming,
        itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
        lastPickedItemContent = { Image(painterResource(it.icon), stringResource(it.title), Modifier.height(32.dp)) },
        onClickOk = { selectedItem ->
            if (element != null) {
                val tagChanges = StringMapChangesBuilder(element.tags)
                selectedItem.applyTo(tagChanges)
                on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
            }
            else if (positionOnWay != null) {
                val action = createNodeAction(positionOnWay, mapDataWithEditsSource) { selectedItem.applyTo(it) }
                if (action != null) {
                    val geometry = ElementPointGeometry(positionOnWay.position)
                    on(Edit(action))
                }
            }
        },
        prefs = preferences,
        favoriteKey = "LaneNarrowingTrafficCalmingForm",
        otherAnswers = { listOfNotNull(
            if (element != null) {
                AnswerItem(stringResource(Res.string.lane_narrowing_traffic_calming_none)) {
                    confirmRemoveLaneNarrowingTrafficCalming = true
                }
            } else null
        ) }
    )

    if (confirmRemoveLaneNarrowingTrafficCalming) {
        AreYouSureDialog(
            onDismissRequest = { confirmRemoveLaneNarrowingTrafficCalming = false },
            onConfirmed = {
                if (element == null) return@AreYouSureDialog
                val tagChanges = StringMapChangesBuilder(element.tags)
                (null as LaneNarrowingTrafficCalming?).applyTo(tagChanges)
                on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
            }
        )
    }
}

private val allRoadsFilter by lazy { """
        ways with highway ~ ${ALL_ROADS.joinToString("|")} and area != yes
    """.toElementFilterExpression()
}

private fun MapDataWithEditsSource.getRoadLines(
    bbox: BoundingBox,
): Collection<Pair<Way, List<LatLon>>> {
    val data = getMapDataWithGeometry(bbox)
    return data
        .filter(allRoadsFilter)
        .filterIsInstance<Way>()
        .map { way ->
            val positions = way.nodeIds.map { data.getNode(it)!!.position }
            way to positions
        }.toList()
}
