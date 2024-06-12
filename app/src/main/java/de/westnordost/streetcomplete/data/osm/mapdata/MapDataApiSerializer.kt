package de.westnordost.streetcomplete.data.osm.mapdata

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.math.max

class MapDataApiSerializer {
    private val xml = XML { defaultPolicy { ignoreUnknownChildren() }}

    fun parseMapData(osmXml: String, ignoreRelationTypes: Set<String?>): MutableMapData =
        xml.decodeFromString<ApiOsm>(osmXml).toMapData(ignoreRelationTypes)

    fun parseElementUpdates(diffResultXml: String): Map<ElementKey, ElementUpdateAction> =
        xml.decodeFromString<ApiDiffResult>(diffResultXml).toElementUpdates()

    fun serializeMapDataChanges(changes: MapDataChanges, changesetId: Long): String =
        xml.encodeToString(changes.toApiOsmChange(changesetId))
}

//region Convert OSM API data structure to our data structure

private fun ApiOsm.toMapData(ignoreRelationTypes: Set<String?>) = MutableMapData(
    nodes = nodes.map { it.toNode() },
    ways = ways.map { it.toWay() },
    relations = relations.mapNotNull {
        if (it.type !in ignoreRelationTypes) it.toRelation() else null
    },
).also { it.boundingBox = bounds?.toBoundingBox() }

private fun ApiNode.toNode() = Node(
    id = id,
    position = LatLon(lat, lon),
    tags = tags.toMap(),
    version = version,
    timestampEdited = timestamp.toEpochMilliseconds()
)

private fun ApiWay.toWay() = Way(
    id = id,
    nodeIds = nodes.map { it.ref },
    tags = tags.toMap(),
    version = version,
    timestampEdited = timestamp.toEpochMilliseconds()
)

private fun ApiRelation.toRelation() = Relation(
    id = id,
    members = members.map { it.toRelationMember() },
    tags = tags.toMap(),
    version = version,
    timestampEdited = timestamp.toEpochMilliseconds()
)

private val ApiRelation.type: String? get() = tags.find { it.k == "type" }?.v

private fun ApiRelationMember.toRelationMember() = RelationMember(
    type = ElementType.valueOf(type.uppercase()),
    ref = ref,
    role = role
)

private fun List<ApiTag>.toMap(): Map<String, String> = associate { (k, v) -> k to v }

private fun ApiBoundingBox.toBoundingBox() = BoundingBox(
    minLatitude = minlat,
    minLongitude = minlon,
    maxLatitude = maxlat,
    maxLongitude = maxlon
)

private fun ApiDiffResult.toElementUpdates(): Map<ElementKey, ElementUpdateAction> {
    val result = HashMap<ElementKey, ElementUpdateAction>(nodes.size + ways.size + relations.size)
    result.putAll(nodes.map { it.toDiffElement(ElementType.NODE) })
    result.putAll(ways.map { it.toDiffElement(ElementType.WAY) })
    result.putAll(relations.map { it.toDiffElement(ElementType.RELATION) })
    return result
}

private fun ApiDiffElement.toDiffElement(type: ElementType): Pair<ElementKey, ElementUpdateAction> {
    val action =
        if (newId != null && newVersion != null) UpdateElement(newId, newVersion)
        else DeleteElement
    return ElementKey(type, oldId) to action
}

//endregion

//region Convert our data structure to OSM API data structure

private fun MapDataChanges.toApiOsmChange(changesetId: Long) = ApiOsmChange(
    create = creations.toApiOsm(changesetId),
    modify = modifications.toApiOsm(changesetId),
    delete = deletions.toApiOsm(changesetId)
)

private fun Collection<Element>.toApiOsm(changesetId: Long): ApiOsm? =
    if (isNotEmpty()) ApiOsm(
        nodes = filterIsInstance<Node>().map { it.toApiNode(changesetId) },
        ways = filterIsInstance<Way>().map { it.toApiWay(changesetId) },
        relations = filterIsInstance<Relation>().map { it.toApiRelation(changesetId) }
    ) else null

private fun Node.toApiNode(changesetId: Long) = ApiNode(
    id = id,
    changeset = changesetId,
    version = version,
    timestamp = Instant.fromEpochMilliseconds(timestampEdited),
    lat = position.latitude,
    lon = position.longitude,
    tags = tags.toApiTags()
)

private fun Way.toApiWay(changesetId: Long) = ApiWay(
    id = id,
    changeset = changesetId,
    version = version,
    timestamp = Instant.fromEpochMilliseconds(timestampEdited),
    tags = tags.toApiTags(),
    nodes = nodeIds.map { ApiWayNode(it) }
)

private fun Relation.toApiRelation(changesetId: Long) = ApiRelation(
    id = id,
    changeset = changesetId,
    version = version,
    timestamp = Instant.fromEpochMilliseconds(timestampEdited),
    members = members.map { it.toApiRelationMember() },
    tags = tags.toApiTags()
)

private fun RelationMember.toApiRelationMember() = ApiRelationMember(
    type = type.name.lowercase(),
    ref = ref,
    role = role
)

private fun Map<String, String>.toApiTags(): List<ApiTag> = map { (k, v) -> ApiTag(k, v) }

//endregion

//region OSM API data structure

@Serializable
@XmlSerialName("diffResult")
private data class ApiDiffResult(
    @XmlChildrenName("node") val nodes: List<ApiDiffElement>,
    @XmlChildrenName("way") val ways: List<ApiDiffElement>,
    @XmlChildrenName("relation") val relations: List<ApiDiffElement>,
)

@Serializable
private data class ApiDiffElement(
    @XmlSerialName("old_id") val oldId: Long,
    @XmlSerialName("new_id") val newId: Long? = null,
    @XmlSerialName("new_version") val newVersion: Int? = null,
)

@Serializable
@XmlSerialName("osmChange")
private data class ApiOsmChange(
    @XmlSerialName("create") val create: ApiOsm? = null,
    @XmlSerialName("modify") val modify: ApiOsm? = null,
    @XmlSerialName("delete") val delete: ApiOsm? = null,
)

@Serializable
@XmlSerialName("osm")
private data class ApiOsm(
    val bounds: ApiBoundingBox? = null,
    @XmlChildrenName("node") val nodes: List<ApiNode>,
    @XmlChildrenName("way") val ways: List<ApiWay>,
    @XmlChildrenName("relation") val relations: List<ApiRelation>,
)


@Serializable
@XmlSerialName("bounds")
private data class ApiBoundingBox(
    val minlat: Double,
    val minlon: Double,
    val maxlat: Double,
    val maxlon: Double
)

@Serializable
@XmlSerialName("node")
private data class ApiNode(
    val id: Long,
    val changeset: Long? = null,
    val version: Int,
    val timestamp: Instant,
    val lat: Double,
    val lon: Double,
    val tags: List<ApiTag> = emptyList(),
)

@Serializable
@XmlSerialName("way")
private data class ApiWay(
    val id: Long,
    val changeset: Long? = null,
    val version: Int,
    val timestamp: Instant,
    val tags: List<ApiTag> = emptyList(),
    val nodes: List<ApiWayNode> = emptyList(),
)

@Serializable
@XmlSerialName("nd")
private data class ApiWayNode(val ref: Long)

@Serializable
@XmlSerialName("relation")
private data class ApiRelation(
    val id: Long,
    val changeset: Long? = null,
    val version: Int,
    val timestamp: Instant,
    val members: List<ApiRelationMember> = emptyList(),
    val tags: List<ApiTag> = emptyList(),
)

@Serializable
@XmlSerialName("member")
private data class ApiRelationMember(
    val type: String,
    val ref: Long,
    val role: String,
)

@Serializable
@XmlSerialName("tag")
private data class ApiTag(val k: String, val v: String)

// endregion
