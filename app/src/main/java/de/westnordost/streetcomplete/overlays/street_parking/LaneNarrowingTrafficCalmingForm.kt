package de.westnordost.streetcomplete.overlays.street_parking

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
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
    private var roads: Collection<Pair<Way, List<LatLon>>>? = null
    private val allRoadsFilter = """
        ways with highway ~ ${ALL_ROADS.joinToString("|")} and area != yes
    """.toElementFilterExpression()

    override val otherAnswers get() = listOfNotNull(
        if (element != null) {
            AnswerItem(R.string.lane_narrowing_traffic_calming_none) {
                confirmRemoveLaneNarrowingTrafficCalming()
            }
        } else null
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (element == null) {
            view.doOnLayout {
                initCreatingPointOnWay()
                checkCurrentCursorPosition()
            }
        }

        setMarkerIcon(R.drawable.ic_quest_choker)
        setMarkerVisibility(false)

        originalLaneNarrowingTrafficCalming = element?.tags?.let { createNarrowingTrafficCalming(it) }
        selectedItem = originalLaneNarrowingTrafficCalming?.asItem()
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
        val maxDistance = metersPerPixel * requireContext().dpToPx(24)
        val snapToVertexDistance = metersPerPixel * requireContext().dpToPx(12)
        positionOnWay = geometry.center.getPositionOnWays(roads, maxDistance, snapToVertexDistance)
        checkIsFormComplete()
    }

    override fun isFormComplete(): Boolean =
        super.isFormComplete() && (element != null || positionOnWay != null)

    override fun hasChanges(): Boolean =
        selectedItem?.value != originalLaneNarrowingTrafficCalming

    override fun onClickOk() {
        val answer = selectedItem!!.value!!
        val element = element
        val positionOnWay = positionOnWay
        if (element != null) {
            val tagChanges = StringMapChangesBuilder(element.tags)
            answer.applyTo(tagChanges)
            applyEdit(UpdateElementTagsAction(element, tagChanges.create()))
        } else if (positionOnWay != null) {
            val action = createNodeAction(positionOnWay, mapDataWithEditsSource) { answer.applyTo(it) } ?: return
            val geometry = ElementPointGeometry(positionOnWay.position)
            applyEdit(action, geometry)
        }
    }

    private fun confirmRemoveLaneNarrowingTrafficCalming() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                val tagChanges = StringMapChangesBuilder(element!!.tags)
                (null as LaneNarrowingTrafficCalming?).applyTo(tagChanges)
                applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
