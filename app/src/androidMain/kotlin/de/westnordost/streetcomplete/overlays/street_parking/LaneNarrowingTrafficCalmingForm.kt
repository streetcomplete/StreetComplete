package de.westnordost.streetcomplete.overlays.street_parking

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnLayout
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.createNodeAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.traffic_calming.LaneNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.traffic_calming.applyTo
import de.westnordost.streetcomplete.osm.traffic_calming.icon
import de.westnordost.streetcomplete.osm.traffic_calming.parseNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.traffic_calming.title
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class LaneNarrowingTrafficCalmingForm : AbstractOverlayForm(), IsMapPositionAware {

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        var confirmRemoveLaneNarrowingTrafficCalming by remember { mutableStateOf(false) }

        ItemSelectOverlayForm(
            itemsPerRow = 2,
            items = LaneNarrowingTrafficCalming.entries,
            initialSelectedItem = remember { element?.tags?.let { parseNarrowingTrafficCalming(it) } },
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            lastPickedItemContent = { Image(painterResource(it.icon), stringResource(it.title), Modifier.height(32.dp)) },
            onClickOk = { selectedItem ->
                val element = element
                val positionOnWay = positionOnWay
                if (element != null) {
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    selectedItem.applyTo(tagChanges)
                    applyEdit(UpdateElementTagsAction(element, tagChanges.create()))
                } else if (positionOnWay != null) {
                    val action = createNodeAction(positionOnWay, mapDataWithEditsSource) { selectedItem.applyTo(it) }
                    if (action != null) {
                        val geometry = ElementPointGeometry(positionOnWay.position)
                        applyEdit(action, geometry)
                    }
                }
            },
            prefs = prefs,
            favoriteKey = "LaneNarrowingTrafficCalmingForm",
            otherAnswers = listOfNotNull(
                if (element != null) {
                    Answer(stringResource(Res.string.lane_narrowing_traffic_calming_none)) {
                        confirmRemoveLaneNarrowingTrafficCalming = true
                    }
                } else {
                    null
                }
            )
        )

        if (confirmRemoveLaneNarrowingTrafficCalming) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmRemoveLaneNarrowingTrafficCalming = false },
                onConfirmed = {
                    val tagChanges = StringMapChangesBuilder(element!!.tags)
                    (null as LaneNarrowingTrafficCalming?).applyTo(tagChanges)
                    applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
                }
            )
        }
    }

    private var originalLaneNarrowingTrafficCalming: LaneNarrowingTrafficCalming? = null

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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkCurrentCursorPosition()
    }

    override fun onMapMoved(position: LatLon) {
        if (element != null) return
        checkCurrentCursorPosition()
    }

    private fun checkCurrentCursorPosition() {
        val roads = roads ?: return
        val metersPerPixel = metersPerPixel ?: return
        val maxDistance = metersPerPixel * resources.dpToPx(24)
        val snapToVertexDistance = metersPerPixel * resources.dpToPx(12)
        positionOnWay = geometry.center.getPositionOnWays(roads, maxDistance, snapToVertexDistance)
        checkIsFormComplete()
    }

    override fun isFormComplete(): Boolean =
        super.isFormComplete() && (element != null || positionOnWay != null)
}
