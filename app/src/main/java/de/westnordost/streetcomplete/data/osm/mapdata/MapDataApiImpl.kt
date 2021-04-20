package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.MapDataApi as OsmapiMapDataApi
import de.westnordost.osmapi.map.data.Element as OsmApiElement
import de.westnordost.osmapi.map.data.Node as OsmApiNode
import de.westnordost.osmapi.map.data.Way as OsmApiWay
import de.westnordost.osmapi.map.data.Relation as OsmApiRelation
import de.westnordost.osmapi.map.data.RelationMember as OsmApiRelationMember
import de.westnordost.osmapi.map.data.BoundingBox as OsmApiBoundingBox
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmRelationMember
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.streetcomplete.data.osmnotes.toBoundingBox
import de.westnordost.streetcomplete.data.osmnotes.toLatLon
import de.westnordost.streetcomplete.data.osmnotes.toOsmApiBoundingBox
import de.westnordost.streetcomplete.data.osmnotes.toOsmLatLon
import java.time.Instant

class MapDataApiImpl(osm: OsmConnection) : MapDataApi {

    private val api: OsmapiMapDataApi = OsmapiMapDataApi(osm)

    override fun uploadChanges(changesetId: Long, elements: Collection<Element>): MapDataUpdates {
        val handler = UpdatedElementsHandler()
        val osmElements = elements.map { it.toOsmApiElement() }
        api.uploadChanges(changesetId, osmElements, handler)
        return handler.getElementUpdates(osmElements.map { it.toElement() })
    }

    override fun openChangeset(tags: Map<String, String?>): Long = api.openChangeset(tags)

    override fun closeChangeset(changesetId: Long) = api.closeChangeset(changesetId)

    override fun getMap(bounds: BoundingBox, mutableMapData: MutableMapData) =
        api.getMap(bounds.toOsmApiBoundingBox(), MapDataApiHandler(mutableMapData))

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

// TODO(Flo): Make this private
fun Element.toOsmApiElement(): OsmApiElement = when(this) {
    is Node -> toOsmApiNode()
    is Way -> toOsmApiWay()
    is Relation -> toOsmApiRelation()
}

// TODO(Flo): Make this private
fun OsmApiElement.toElement(): Element = when(this) {
    is OsmApiNode -> toNode()
    is OsmApiWay -> toWay()
    is OsmApiRelation -> toRelation()
    else -> throw IllegalArgumentException()
}

// TODO(Flo): Make this private
fun Node.toOsmApiNode(): OsmApiNode = OsmNode(
    id,
    version,
    position.toOsmLatLon(),
    tags,
    null,
    Instant.ofEpochMilli(timestampEdited)
)

// TODO(Flo): Make this private
fun OsmApiNode.toNode(): Node = Node(id, position.toLatLon(), tags, version, editedAt.toEpochMilli())

// TODO(Flo): Make this private
fun Way.toOsmApiWay(): OsmApiWay = OsmWay(
    id,
    version,
    nodeIds,
    tags,
    null,
    Instant.ofEpochMilli(timestampEdited)
)

// TODO(Flo): Make this private
fun OsmApiWay.toWay(): Way = Way(id, nodeIds, tags, version, editedAt.toEpochMilli())

// TODO(Flo): Make this private
fun Relation.toOsmApiRelation(): OsmApiRelation = OsmRelation(
    id,
    version,
    members.map { it.toOsmRelationMember() },
    tags,
    null,
    Instant.ofEpochMilli(timestampEdited)
)

// TODO(Flo): Make this private
fun OsmApiRelation.toRelation(): Relation = Relation(
    id,
    members.map { it.toRelationMember() }.toMutableList(),
    tags,
    version,
    editedAt.toEpochMilli()
)

// TODO(Flo): Make this private
fun RelationMember.toOsmRelationMember(): OsmApiRelationMember = OsmRelationMember(
    ref,
    role,
    type.toOsmElementType()
)

// TODO(Flo): Make this private
fun OsmApiRelationMember.toRelationMember() = RelationMember(type.toElementType(), ref, role)

// TODO(Flo): Make this private
fun ElementType.toOsmElementType(): OsmApiElement.Type = when(this) {
    ElementType.NODE        -> OsmApiElement.Type.NODE
    ElementType.WAY         -> OsmApiElement.Type.WAY
    ElementType.RELATION    -> OsmApiElement.Type.RELATION
}

// TODO(Flo): Make this private
fun OsmApiElement.Type.toElementType(): ElementType = when(this) {
    OsmApiElement.Type.NODE     -> ElementType.NODE
    OsmApiElement.Type.WAY      -> ElementType.WAY
    OsmApiElement.Type.RELATION -> ElementType.RELATION
}

private class MapDataApiHandler(val data: MutableMapData) : MapDataHandler {
    override fun handle(bounds: OsmApiBoundingBox) { data.boundingBox = bounds.toBoundingBox() }
    override fun handle(node: OsmApiNode) { data.add(node.toNode()) }
    override fun handle(way: OsmApiWay) { data.add(way.toWay()) }
    override fun handle(relation: OsmApiRelation) { data.add(relation.toRelation()) }
}
