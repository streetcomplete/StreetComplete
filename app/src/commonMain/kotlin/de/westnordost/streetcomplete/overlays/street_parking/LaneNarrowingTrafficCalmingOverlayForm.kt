package de.westnordost.streetcomplete.overlays.street_parking

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.traffic_calming.LaneNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.traffic_calming.applyTo
import de.westnordost.streetcomplete.osm.traffic_calming.icon
import de.westnordost.streetcomplete.osm.traffic_calming.parseNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.traffic_calming.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LaneNarrowingTrafficCalmingForm(
    on: (OverlayAction) -> Unit,
    element: Element,
    mapDataWithEditsSource: MapDataWithEditsSource = koinInject(),
    preferences: Preferences = koinInject()
) {
    val originalLaneNarrowingTrafficCalming = remember(element) {
        element?.tags?.let { parseNarrowingTrafficCalming(it) }
    }

    var confirmRemoveLaneNarrowingTrafficCalming by remember { mutableStateOf(false) }

    ItemSelectOverlayForm(
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
            /* TODO compose-quest-form position on way stuff
            else if (positionOnWay != null) {
                val action = createNodeAction(positionOnWay, mapDataWithEditsSource) { selectedItem.applyTo(it) }
                if (action != null) {
                    val geometry = ElementPointGeometry(positionOnWay.position)
                    onEdit(action)
                }
            }*/
        },
        prefs = preferences,
        favoriteKey = "LaneNarrowingTrafficCalmingForm",
        on = on,
        otherAnswers = { listOfNotNull(
            if (element != null) {
                AnswerItem(stringResource(Res.string.lane_narrowing_traffic_calming_none)) {
                    confirmRemoveLaneNarrowingTrafficCalming = true
                }
            } else null
        ) }
    )

    if (confirmRemoveLaneNarrowingTrafficCalming) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmRemoveLaneNarrowingTrafficCalming = false },
            onConfirmed = {
                val tagChanges = StringMapChangesBuilder(element.tags)
                (null as LaneNarrowingTrafficCalming?).applyTo(tagChanges)
                on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
            }
        )
    }
}


// TODO compose-quest-form position on way stuff
/*

    private var positionOnWay: PositionOnWay? = null
        set(value) {
            field = value
            if (value != null) {
                setMarkerPosition(value.position)
                setMarkerVisibility(true)
            } else {
                setMarkerVisibility(false)
                setMarkerPosition(null)
            }
        }
    private var roads: Collection<Pair<Way, List<LatLon>>>? = null
    private val allRoadsFilter = """
        ways with highway ~ ${ALL_ROADS.joinToString("|")} and area != yes
    """.toElementFilterExpression()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (element == null) {
            view.doOnLayout {
                initCreatingPointOnWay()
                checkCurrentCursorPosition()
            }
        }

        setMarkerIcon(R.drawable.quest_choker)
        setMarkerVisibility(false)
    }

    private fun initCreatingPointOnWay() {
        val data = mapDataWithEditsSource.getMapDataWithGeometry(geometry.center.enclosingBoundingBox(100.0))
        roads = data
            .filter(allRoadsFilter)
            .filterIsInstance<Way>()
            .map { way ->
                val positions = way.nodeIds.map { data.getNode(it)!!.position }
                way to positions
            }.toList()

override fun isFormComplete(): Boolean =
    super.isFormComplete() && (element != null || positionOnWay != null)

    }
    private fun checkCurrentCursorPosition() {
        val roads = roads ?: return
        val metersPerPixel = metersPerPixel ?: return
        val maxDistance = metersPerPixel * resources.dpToPx(24)
        val snapToVertexDistance = metersPerPixel * resources.dpToPx(12)
        positionOnWay = geometry.center.getPositionOnWays(roads, maxDistance, snapToVertexDistance)
        checkIsFormComplete()
    }
 */
