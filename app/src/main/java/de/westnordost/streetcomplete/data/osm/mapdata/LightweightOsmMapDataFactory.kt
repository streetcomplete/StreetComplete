package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.changesets.Changeset
import de.westnordost.osmapi.map.MapDataFactory
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmRelationMember
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.RelationMember
import de.westnordost.osmapi.map.data.Way
import java.util.*

/** Same as OsmMapDataFactory only that it throws away the Changeset data included in the OSM
 *  response */
class LightweightOsmMapDataFactory : MapDataFactory {
    override fun createNode(
        id: Long, version: Int, lat: Double, lon: Double, tags: MutableMap<String, String>?,
        changeset: Changeset?, dateEdited: Date?
    ): Node = OsmNode(id, version, lat, lon, tags, null, dateEdited)

    override fun createWay(
        id: Long, version: Int, nodes: MutableList<Long>, tags: MutableMap<String, String>?,
        changeset: Changeset?, dateEdited: Date?
    ): Way = OsmWay(id, version, nodes, tags, null, dateEdited)

    override fun createRelation(
        id: Long, version: Int, members: MutableList<RelationMember>,
        tags: MutableMap<String, String>?, changeset: Changeset?, dateEdited: Date?
    ): Relation = OsmRelation(id, version, members, tags, null, dateEdited)

    override fun createRelationMember(
        ref: Long, role: String?, type: Element.Type
    ): RelationMember = OsmRelationMember(ref, role, type)
}
