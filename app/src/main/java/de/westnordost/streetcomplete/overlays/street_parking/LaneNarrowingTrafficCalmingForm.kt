package de.westnordost.streetcomplete.overlays.street_parking

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeAction
import de.westnordost.streetcomplete.data.osm.edits.create.CreateNodeFromVertexAction
import de.westnordost.streetcomplete.data.osm.edits.create.InsertIntoWayAt
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.applyTo
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.asItem
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.createNarrowingTrafficCalming
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.math.PositionOnWay
import de.westnordost.streetcomplete.util.math.PositionOnWaySegment
import de.westnordost.streetcomplete.util.math.VertexOfWay
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPositionOnWays
import org.koin.android.ext.android.inject

class LaneNarrowingTrafficCalmingForm :
    AImageSelectOverlayForm<LaneNarrowingTrafficCalming>(), IsMapPositionAware {

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()

    override val items get() = LaneNarrowingTrafficCalming.values().map { it.asItem() }

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
    private var roads: Collection<Pair<Way, ElementGeometry>>? = null
    private val allRoadsFilter = """
        ways with highway ~ ${ALL_ROADS.joinToString("|")}
    """.toElementFilterExpression()

    override val otherAnswers get() = listOfNotNull(
        if (element != null) {
            AnswerItem(R.string.lane_narrowing_traffic_calming_none) { confirmRemoveLaneNarrowingTrafficCalming() }
        } else {
            null
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (element == null) {
            initCreatingPointOnWay()
            checkCurrentCursorPosition()
        }

        setMarkerIcon(R.drawable.ic_quest_choker)
        setMarkerVisibility(false)

        originalLaneNarrowingTrafficCalming = element?.tags?.let { createNarrowingTrafficCalming(it) }
        selectedItem = originalLaneNarrowingTrafficCalming?.asItem()
    }

    private fun initCreatingPointOnWay() {
        val data = mapDataWithEditsSource.getMapDataWithGeometry(geometry.center.enclosingBoundingBox(150.0))
        roads = data
            .filter(allRoadsFilter)
            .mapNotNull { element ->
                if (element !is Way) return@mapNotNull null
                val geometry = data.getWayGeometry(element.id) ?: return@mapNotNull null
                element to geometry
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
        val maxDistance = metersPerPixel * requireContext().dpToPx(32)
        val snapToVertexDistance = metersPerPixel * requireContext().dpToPx(12)
        positionOnWay = geometry.center.getPositionOnWays(roads, maxDistance, snapToVertexDistance)
        checkIsFormComplete()
    }

    override fun isFormComplete(): Boolean =
        super.isFormComplete() && (element != null || positionOnWay != null)

    override fun hasChanges(): Boolean =
        selectedItem?.value != originalLaneNarrowingTrafficCalming

    override fun onClickOk() {
        val narrowingTrafficCalming = selectedItem!!.value!!
        val element = element
        val pointOnWay = positionOnWay
        if (element != null) {
            val tagChanges = createChanges(narrowingTrafficCalming, element.tags)
            applyEdit(UpdateElementTagsAction(element, tagChanges.create()))
        } else if (pointOnWay != null) {
            val geometry = ElementPointGeometry(pointOnWay.position)
            when (pointOnWay) {
                is PositionOnWaySegment -> {
                    val insertIntoWayAt = InsertIntoWayAt(
                        pointOnWay.wayId,
                        pointOnWay.segment.first,
                        pointOnWay.segment.second
                    )
                    val tags = createChanges(narrowingTrafficCalming, mapOf())
                    applyEdit(CreateNodeAction(pointOnWay.position, tags, listOf(insertIntoWayAt)), geometry)
                }
                is VertexOfWay -> {
                    val node = mapDataWithEditsSource.getNode(pointOnWay.nodeId) ?: return
                    val tagChanges = createChanges(narrowingTrafficCalming, node.tags)
                    val containingWayIds = mapDataWithEditsSource.getWaysForNode(pointOnWay.nodeId).map { it.id }
                    applyEdit(CreateNodeFromVertexAction(node, tagChanges.create(), containingWayIds), geometry)
                }
            }
        }
    }

    private fun createChanges(value: LaneNarrowingTrafficCalming?, source: Map<String, String>): StringMapChangesBuilder {
        val tagChanges = StringMapChangesBuilder(source)
        value.applyTo(tagChanges)
        return tagChanges
    }

    private fun confirmRemoveLaneNarrowingTrafficCalming() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                val tagChanges = createChanges(null, element!!.tags)
                applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
