package de.westnordost.streetcomplete.data.osm

import java.util.Date

import de.westnordost.streetcomplete.data.Quest
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.upload.HasElementTagChanges
import de.westnordost.streetcomplete.data.osm.upload.UploadableInChangeset
import de.westnordost.streetcomplete.util.measuredLength
import de.westnordost.streetcomplete.util.pointOnPolylineFromEnd
import de.westnordost.streetcomplete.util.pointOnPolylineFromStart

/** Represents one task for the user to complete/correct the data based on one OSM element  */
data class OsmQuest(
    override var id: Long?,
    override val osmElementQuestType: OsmElementQuestType<*>, // underlying OSM data
    override val elementType: Element.Type,
    override val elementId: Long,
    override var status: QuestStatus,
    override var changes: StringMapChanges?,
    var changesSource: String?,
    override var lastUpdate: Date,
    override val geometry: ElementGeometry
) : Quest, UploadableInChangeset, HasElementTagChanges {

    constructor(type: OsmElementQuestType<*>, elementType: Element.Type, elementId: Long, geometry: ElementGeometry)
        : this(null, type, elementType, elementId, QuestStatus.NEW, null, null, Date(), geometry)
    
    override val center: LatLon get() = geometry.center
    override val type: QuestType<*> get() = osmElementQuestType

    override val markerLocations: Array<LatLon> get() {
        if (osmElementQuestType.hasMarkersAtEnds && geometry is ElementPolylinesGeometry) {
            val polyline = geometry.polylines[0]
            val length = polyline.measuredLength()
            if (length > 15 * 4) {
                return arrayOf(
                    polyline.pointOnPolylineFromStart(15.0)!!,
                    polyline.pointOnPolylineFromEnd(15.0)!!
                )
            }
        }
        return arrayOf(center)
    }

    override fun isApplicableTo(element: Element) = osmElementQuestType.isApplicableTo(element)

    fun solve(changes: StringMapChanges, source: String) {
        this.changes = changes
        this.changesSource = source
        status = QuestStatus.ANSWERED
    }

    fun undo() {
        status = QuestStatus.NEW
        changes = null
        changesSource = null
    }

    fun revert() {
        status = QuestStatus.REVERT
    }

    fun hide() {
        status = QuestStatus.HIDDEN
    }

    fun close() {
        status = QuestStatus.CLOSED
    }

    /* --------------------------- UploadableInChangeset --------------------------- */

    override val source: String get() = changesSource!!
}
