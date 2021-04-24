package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.MapDataApi as OsmApiMapDataApi
import de.westnordost.osmapi.map.data.Element as OsmApiElement
import de.westnordost.osmapi.map.data.Node as OsmApiNode
import de.westnordost.osmapi.map.data.Way as OsmApiWay
import de.westnordost.osmapi.map.data.Relation as OsmApiRelation
import de.westnordost.osmapi.map.data.RelationMember as OsmApiRelationMember
import de.westnordost.osmapi.map.data.BoundingBox as OsmApiBoundingBox
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmRelationMember
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.handler.MapDataHandler
import java.time.Instant

class MapDataApiImpl(osm: OsmConnection) : MapDataApi {

    private val api: OsmApiMapDataApi = OsmApiMapDataApi(osm)

    override fun uploadChanges(changesetId: Long, elements: Collection<Element>): MapDataUpdates {
        val handler = UpdatedElementsHandler()
        val osmElements = elements.map { it.toOsmApiElement() }
        api.uploadChanges(changesetId, osmElements) {
            if (it.type !== null) handler.handle(DiffElement(
                it.type.toElementType(),
                it.clientId,
                it.serverId,
                it.serverVersion
            ))
        }
        return handler.getElementUpdates(osmElements.map { it.toElement() })
    }

    override fun openChangeset(tags: Map<String, String?>): Long = api.openChangeset(tags)

    override fun closeChangeset(changesetId: Long) = api.closeChangeset(changesetId)

    override fun getMap(bounds: BoundingBox, mutableMapData: MutableMapData) = api.getMap(
        OsmApiBoundingBox(bounds.min.latitude, bounds.min.longitude, bounds.max.latitude, bounds.max.longitude),
        MapDataApiHandler(mutableMapData)
    )

    override fun getWayComplete(id: Long): MapData? =
        try {
            val result = MutableMapData()
            api.getWayComplete(id, MapDataApiHandler(result))
            result
        } catch (e: OsmNotFoundException) {
            null
        }

    override fun getRelationComplete(id: Long): MapData? =
        try {
            val result = MutableMapData()
            api.getRelationComplete(id, MapDataApiHandler(result))
            result
        } catch (e: OsmNotFoundException) {
            null
        }

    override fun getNode(id: Long): Node? = api.getNode(id)?.toNode()

    override fun getWay(id: Long): Way? = api.getWay(id)?.toWay()

    override fun getRelation(id: Long): Relation? = api.getRelation(id)?.toRelation()

    override fun getWaysForNode(id: Long): List<Way> =
        api.getWaysForNode(id).map { it.toWay() }

    override fun getRelationsForNode(id: Long): List<Relation> =
        api.getRelationsForNode(id).map { it.toRelation() }

    override fun getRelationsForWay(id: Long): List<Relation> =
        api.getRelationsForWay(id).map { it.toRelation() }

    override fun getRelationsForRelation(id: Long): List<Relation> =
        api.getRelationsForRelation(id).map { it.toRelation() }
}

private fun Element.toOsmApiElement(): OsmApiElement = when(this) {
    is Node -> toOsmApiNode()
    is Way -> toOsmApiWay()
    is Relation -> toOsmApiRelation()
}

private fun OsmApiElement.toElement(): Element = when(this) {
    is OsmApiNode -> toNode()
    is OsmApiWay -> toWay()
    is OsmApiRelation -> toRelation()
    else -> throw IllegalArgumentException()
}

private fun Node.toOsmApiNode(): OsmApiNode = OsmNode(
    id,
    version,
    OsmLatLon(position.latitude, position.longitude),
    tags,
    null,
    Instant.ofEpochMilli(timestampEdited)
)

private fun OsmApiNode.toNode(): Node =
    Node(id, LatLon(position.latitude, position.longitude), tags, version, editedAt.toEpochMilli())

private fun Way.toOsmApiWay(): OsmApiWay = OsmWay(
    id,
    version,
    nodeIds,
    tags,
    null,
    Instant.ofEpochMilli(timestampEdited)
)

private fun OsmApiWay.toWay(): Way = Way(id, nodeIds, tags, version, editedAt.toEpochMilli())

private fun Relation.toOsmApiRelation(): OsmApiRelation = OsmRelation(
    id,
    version,
    members.map { it.toOsmRelationMember() },
    tags,
    null,
    Instant.ofEpochMilli(timestampEdited)
)

private fun OsmApiRelation.toRelation(): Relation = Relation(
    id,
    members.map { it.toRelationMember() }.toMutableList(),
    tags,
    version,
    editedAt.toEpochMilli()
)

private fun RelationMember.toOsmRelationMember(): OsmApiRelationMember = OsmRelationMember(
    ref,
    role,
    type.toOsmElementType()
)

private fun OsmApiRelationMember.toRelationMember() =
    RelationMember(type.toElementType(), ref, role)

private fun ElementType.toOsmElementType(): OsmApiElement.Type = when(this) {
    ElementType.NODE        -> OsmApiElement.Type.NODE
    ElementType.WAY         -> OsmApiElement.Type.WAY
    ElementType.RELATION    -> OsmApiElement.Type.RELATION
}

private fun OsmApiElement.Type.toElementType(): ElementType = when(this) {
    OsmApiElement.Type.NODE     -> ElementType.NODE
    OsmApiElement.Type.WAY      -> ElementType.WAY
    OsmApiElement.Type.RELATION -> ElementType.RELATION
}

private class MapDataApiHandler(val data: MutableMapData) : MapDataHandler {
    override fun handle(bounds: OsmApiBoundingBox) {
        data.boundingBox = BoundingBox(
            bounds.minLatitude,
            bounds.minLongitude,
            bounds.maxLatitude,
            bounds.maxLongitude
        )
    }
    override fun handle(node: OsmApiNode) { data.add(node.toNode()) }
    override fun handle(way: OsmApiWay) { data.add(way.toWay()) }
    override fun handle(relation: OsmApiRelation) { data.add(relation.toRelation()) }
}
