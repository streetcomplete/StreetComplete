package de.westnordost.streetcomplete.data.osm.mapdata

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.EventType.*
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.xmlStreaming

class MapDataApiSerializer {
    private val xml = XML { defaultPolicy { ignoreUnknownChildren() } }

    fun parseMapData(osmXml: String, ignoreRelationTypes: Set<String?>): MutableMapData =
        xmlStreaming.newReader(osmXml).parseMapData(ignoreRelationTypes)

    fun parseElementUpdates(diffResultXml: String): Map<ElementKey, ElementUpdateAction> =
        xml.decodeFromString<ApiDiffResult>(diffResultXml).toElementUpdates()

    fun serializeMapDataChanges(changes: MapDataChanges, changesetId: Long): String =
        xml.encodeToString(changes.toApiOsmChange(changesetId))
}

private fun XmlReader.parseMapData(ignoreRelationTypes: Set<String?>): MutableMapData = try {
    val result = MutableMapData()
    var tags: MutableMap<String, String>? = null
    var nodes: MutableList<Long> = ArrayList()
    var members: MutableList<RelationMember> = ArrayList()
    var id: Long? = null
    var position: LatLon? = null
    var version: Int? = null
    var timestamp: Long? = null

    forEach { when (it) {
        START_ELEMENT -> when (localName) {
            "tag" -> {
                if (tags == null) tags = HashMap()
                tags!![attribute("k")] = attribute("v")
            }
            "nd" -> nodes.add(attribute("ref").toLong())
            "member" -> members.add(RelationMember(
                type = ElementType.valueOf(attribute("type").uppercase()),
                ref = attribute("ref").toLong(),
                role = attribute("role")
            ))
            "bounds" -> result.boundingBox = BoundingBox(
                minLatitude = attribute("minlat").toDouble(),
                minLongitude = attribute("minlon").toDouble(),
                maxLatitude = attribute("maxlat").toDouble(),
                maxLongitude = attribute("maxlon").toDouble()
            )
            "node", "way", "relation" -> {
                id = attribute("id").toLong()
                version = attribute("version").toInt()
                timestamp = Instant.parse(attribute("timestamp")).toEpochMilliseconds()
                if (localName == "node") {
                    position = LatLon(attribute("lat").toDouble(), attribute("lon").toDouble())
                }
            }
        }
        END_ELEMENT -> when (localName) {
            "node" -> {
                result.add(Node(id!!, position!!, tags.orEmpty(), version!!, timestamp!!))
                tags = null
            }
            "way" -> {
                result.add(Way(id!!, nodes, tags.orEmpty(), version!!, timestamp!!))
                nodes = ArrayList()
                tags = null
            }
            "relation" -> {
                if (tags.orEmpty()["type"] !in ignoreRelationTypes) {
                    result.add(Relation(id!!, members, tags.orEmpty(), version!!, timestamp!!))
                }
                members = ArrayList()
                tags = null
            }
        }
        else -> {}
    } }
    result
} catch (e: Exception) { throw SerializationException(e) }

private fun XmlReader.attribute(name: String): String = getAttributeValue(null, name)!!

private fun XmlReader.attributeOrNull(name: String): String? = getAttributeValue(null, name)

//region Convert OSM API data structure to our data structure

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
    @XmlSerialName("node") val nodes: List<ApiDiffElement>,
    @XmlSerialName("way") val ways: List<ApiDiffElement>,
    @XmlSerialName("relation") val relations: List<ApiDiffElement>,
)

@Serializable
private open class ApiDiffElement(
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
    val nodes: List<ApiNode>,
    val ways: List<ApiWay>,
    val relations: List<ApiRelation>,
)

@Serializable
@XmlSerialName("node")
private data class ApiNode(
    val id: Long,
    val version: Int,
    val changeset: Long? = null,
    val timestamp: Instant,
    val lat: Double,
    val lon: Double,
    val tags: List<ApiTag> = emptyList(),
)

@Serializable
@XmlSerialName("way")
private data class ApiWay(
    val id: Long,
    val version: Int,
    val changeset: Long? = null,
    val timestamp: Instant,
    val nodes: List<ApiWayNode> = emptyList(),
    val tags: List<ApiTag> = emptyList(),
)

@Serializable
@XmlSerialName("nd")
private data class ApiWayNode(val ref: Long)

@Serializable
@XmlSerialName("relation")
private data class ApiRelation(
    val id: Long,
    val version: Int,
    val changeset: Long? = null,
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
