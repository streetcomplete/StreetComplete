package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.common.errors.OsmApiReadResponseException
import de.westnordost.osmapi.common.errors.OsmAuthorizationException
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmConnectionException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException
import de.westnordost.osmapi.map.data.OsmElement
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmRelationMember
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.streetcomplete.data.download.ConnectionException
import de.westnordost.streetcomplete.data.download.QueryTooBigException
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.user.AuthorizationException
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import de.westnordost.osmapi.map.MapDataApi as OsmApiMapDataApi
import de.westnordost.osmapi.map.changes.DiffElement as OsmApiDiffElement
import de.westnordost.osmapi.map.data.BoundingBox as OsmApiBoundingBox
import de.westnordost.osmapi.map.data.Element as OsmApiElement
import de.westnordost.osmapi.map.data.Node as OsmApiNode
import de.westnordost.osmapi.map.data.Relation as OsmApiRelation
import de.westnordost.osmapi.map.data.RelationMember as OsmApiRelationMember
import de.westnordost.osmapi.map.data.Way as OsmApiWay

class MapDataApiImpl(osm: OsmConnection) : MapDataApi {

    private val api: OsmApiMapDataApi = OsmApiMapDataApi(osm)

    override fun uploadChanges(
        changesetId: Long,
        changes: MapDataChanges,
        ignoreRelationTypes: Set<String?>
    ) = wrapExceptions {
        try {
            val handler = UpdatedElementsHandler(ignoreRelationTypes)
            api.uploadChanges(changesetId, changes.toOsmApiElements()) {
                handler.handle(it.toDiffElement())
            }
            val allChangedElements = changes.creations + changes.modifications + changes.deletions
            handler.getElementUpdates(allChangedElements)
        } catch (e: OsmApiException) {
            throw ConflictException(e.message, e)
        }
    }

    override fun openChangeset(tags: Map<String, String?>): Long = wrapExceptions {
        api.openChangeset(tags)
    }

    override fun closeChangeset(changesetId: Long) =
        try {
            wrapExceptions { api.closeChangeset(changesetId) }
        } catch (e: OsmNotFoundException) {
            throw ConflictException(e.message, e)
        }

    override fun getMap(
        bounds: BoundingBox,
        mutableMapData: MutableMapData,
        ignoreRelationTypes: Set<String?>
    ) = wrapExceptions {
        api.getMap(
            bounds.toOsmApiBoundingBox(),
            MapDataApiHandler(mutableMapData, ignoreRelationTypes)
        )
    }

    override fun getWayComplete(id: Long): MapData? =
        try {
            val result = MutableMapData()
            wrapExceptions { api.getWayComplete(id, MapDataApiHandler(result)) }
            result
        } catch (e: OsmNotFoundException) {
            null
        }

    override fun getRelationComplete(id: Long): MapData? =
        try {
            val result = MutableMapData()
            wrapExceptions { api.getRelationComplete(id, MapDataApiHandler(result)) }
            result
        } catch (e: OsmNotFoundException) {
            null
        }

    override fun getNode(id: Long): Node? = wrapExceptions {
        api.getNode(id)?.toNode()
    }

    override fun getWay(id: Long): Way? = wrapExceptions {
        api.getWay(id)?.toWay()
    }

    override fun getRelation(id: Long): Relation? = wrapExceptions {
        api.getRelation(id)?.toRelation()
    }

    override fun getWaysForNode(id: Long): List<Way> = wrapExceptions {
        api.getWaysForNode(id).map { it.toWay() }
    }

    override fun getRelationsForNode(id: Long): List<Relation> = wrapExceptions {
        api.getRelationsForNode(id).map { it.toRelation() }
    }

    override fun getRelationsForWay(id: Long): List<Relation> = wrapExceptions {
        api.getRelationsForWay(id).map { it.toRelation() }
    }

    override fun getRelationsForRelation(id: Long): List<Relation> = wrapExceptions {
        api.getRelationsForRelation(id).map { it.toRelation() }
    }
}

private inline fun <T> wrapExceptions(block: () -> T): T =
    try {
        block()
    } catch (e: OsmAuthorizationException) {
        throw AuthorizationException(e.message, e)
    } catch (e: OsmConflictException) {
        throw ConflictException(e.message, e)
    } catch (e: OsmQueryTooBigException) {
        throw QueryTooBigException(e.message, e)
    } catch (e: OsmConnectionException) {
        throw ConnectionException(e.message, e)
    } catch (e: OsmApiReadResponseException) {
        // probably a temporary connection error
        throw ConnectionException(e.message, e)
    } catch (e: OsmApiException) {
        // request timeout is a temporary connection error
        throw if (e.errorCode == 408) ConnectionException(e.message, e) else e
    }

/* --------------------------------- Element -> OsmApiElement ----------------------------------- */

private fun MapDataChanges.toOsmApiElements(): List<OsmApiElement> =
    creations.map { it.toOsmApiElement().apply { isNew = true } } +
    modifications.map { it.toOsmApiElement().apply { isModified = true } } +
    deletions.map { it.toOsmApiElement().apply { isDeleted = true } }

private fun Element.toOsmApiElement(): OsmElement = when (this) {
    is Node -> toOsmApiNode()
    is Way -> toOsmApiWay()
    is Relation -> toOsmApiRelation()
}

private fun Node.toOsmApiNode() = OsmNode(
    id,
    version,
    OsmLatLon(position.latitude, position.longitude),
    tags,
    null,
    Instant.fromEpochMilliseconds(timestampEdited).toJavaInstant()
)

private fun Way.toOsmApiWay() = OsmWay(
    id,
    version,
    nodeIds,
    tags,
    null,
    Instant.fromEpochMilliseconds(timestampEdited).toJavaInstant()
)

private fun Relation.toOsmApiRelation() = OsmRelation(
    id,
    version,
    members.map { it.toOsmRelationMember() },
    tags,
    null,
    Instant.fromEpochMilliseconds(timestampEdited).toJavaInstant()
)

private fun RelationMember.toOsmRelationMember() = OsmRelationMember(
    ref,
    role,
    type.toOsmElementType()
)

private fun ElementType.toOsmElementType(): OsmApiElement.Type = when (this) {
    ElementType.NODE        -> OsmApiElement.Type.NODE
    ElementType.WAY         -> OsmApiElement.Type.WAY
    ElementType.RELATION    -> OsmApiElement.Type.RELATION
}

private fun BoundingBox.toOsmApiBoundingBox() =
    OsmApiBoundingBox(min.latitude, min.longitude, max.latitude, max.longitude)

/* --------------------------------- OsmApiElement -> Element ----------------------------------- */

private fun OsmApiNode.toNode() =
    Node(id, LatLon(position.latitude, position.longitude), HashMap(tags), version, editedAt.toEpochMilli())

private fun OsmApiWay.toWay() =
    Way(id, ArrayList(nodeIds), HashMap(tags), version, editedAt.toEpochMilli())

private fun OsmApiRelation.toRelation() = Relation(
    id,
    members.map { it.toRelationMember() }.toMutableList(),
    HashMap(tags),
    version,
    editedAt.toEpochMilli()
)

private fun OsmApiRelationMember.toRelationMember() =
    RelationMember(type.toElementType(), ref, role)

private fun OsmApiElement.Type.toElementType(): ElementType = when (this) {
    OsmApiElement.Type.NODE     -> ElementType.NODE
    OsmApiElement.Type.WAY      -> ElementType.WAY
    OsmApiElement.Type.RELATION -> ElementType.RELATION
}

private fun OsmApiDiffElement.toDiffElement() = DiffElement(
    type.toElementType(),
    clientId,
    serverId,
    serverVersion
)

private fun OsmApiBoundingBox.toBoundingBox() =
    BoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude)

/* ---------------------------------------------------------------------------------------------- */

private class MapDataApiHandler(
    val data: MutableMapData,
    val ignoreRelationTypes: Set<String?> = emptySet()
) : MapDataHandler {

    override fun handle(bounds: OsmApiBoundingBox) {
        data.boundingBox = bounds.toBoundingBox()
    }

    override fun handle(node: OsmApiNode) {
        data.add(node.toNode())
    }

    override fun handle(way: OsmApiWay) {
        data.add(way.toWay())
    }

    override fun handle(relation: OsmApiRelation) {
        val relationType = relation.tags?.get("type")
        if (relationType !in ignoreRelationTypes) {
            data.add(relation.toRelation())
        }
    }
}
