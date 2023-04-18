package de.westnordost.streetcomplete.overlays.street_parking

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.applyTo
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.asItem
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.createNarrowingTrafficCalming
import de.westnordost.streetcomplete.overlays.AImageSelectOverlayForm
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.PointOnWay
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.getPointOnWays
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LaneNarrowingTrafficCalmingForm :
    AImageSelectOverlayForm<LaneNarrowingTrafficCalming>(), IsMapPositionAware {

    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()

    override val items get() = LaneNarrowingTrafficCalming.values().map { it.asItem() }

    private var originalLaneNarrowingTrafficCalming: LaneNarrowingTrafficCalming? = null

    private var pointOnWay: PointOnWay? = null
    private var roadPolylinesByWayId: Map<Long, List<LatLon>>? = null
    private val allRoadsFilter = """
        ways with highway ~ ${ALL_ROADS.joinToString("|")}
    """.toElementFilterExpression()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (element == null && geometry is ElementPointGeometry) {
            viewLifecycleScope.launch {
                val center = geometry.center
                val data = mapDataWithEditsSource.getMapDataWithGeometry(center.enclosingBoundingBox(100.0))
                roadPolylinesByWayId = data
                    .filter(allRoadsFilter)
                    .mapNotNull {
                        val polyline =
                            (data.getWayGeometry(it.id) as? ElementPolylinesGeometry)?.polylines?.singleOrNull()
                        if (polyline == null) null else it.id to polyline
                    }.toMap()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalLaneNarrowingTrafficCalming = element?.tags?.let { createNarrowingTrafficCalming(it) }
        selectedItem = originalLaneNarrowingTrafficCalming?.asItem()
    }

    override fun onMapMoved(position: LatLon) {
        if (element != null) return
        val geometriesByWayId = roadPolylinesByWayId ?: return

        pointOnWay = position.getPointOnWays(geometriesByWayId, maxDistance, snaptoVertexDistance)
        setMarkerIcon(....)
        TODO("Not yet implemented")
    }

    override fun isFormComplete(): Boolean =
        super.isFormComplete() && (element != null || pointOnWay != null)

    override fun hasChanges(): Boolean =
        selectedItem?.value != originalLaneNarrowingTrafficCalming

    override fun onClickOk() {
        val narrowingTrafficCalming = selectedItem!!.value!!
        val element = element
        if (element != null) {
            val tagChanges = StringMapChangesBuilder(element.tags)
            narrowingTrafficCalming.applyTo(tagChanges)
            applyEdit(UpdateElementTagsAction(tagChanges.create()))
        } else if (pointOnWay != null) {
            // TODO
        }
    }
}
