package de.westnordost.streetcomplete.data.osm.splitway

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.upload.UploadableInChangeset

/** Contains all necessary information about where to perform a split of a certain OSM way.
 *
 *  It is assigned to a quest and source because it should be put in the same changeset as the
 *  quest normally would, so that the context in which a way was split is clear for people doing
 *  QA.
 *
 *  Keeping the split positions as a lat-lon position because it more robust when handling
 *  conflicts than if the split positions were kept as node ids or node indices of the way.
 *  */
data class OsmQuestSplitWay(
        val questId: Long,
        val questType: OsmElementQuestType<*>,
        val wayId: Long,
        override val source: String,
        val splits: List<SplitPolylineAtPosition>,
        val questTypesOnWay: List<OsmElementQuestType<*>>) : UploadableInChangeset {

    override val osmElementQuestType get() = questType
    override val elementType get() = Element.Type.WAY
    override val elementId get() = wayId
    override val position: LatLon get() = splits.first().pos
}
