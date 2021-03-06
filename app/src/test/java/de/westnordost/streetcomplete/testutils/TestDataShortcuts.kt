package de.westnordost.streetcomplete.testutils

import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.osmapi.user.User
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import java.util.*

fun p(lat: Double = 0.0, lon: Double = 0.0) = OsmLatLon(lat, lon)

fun node(
    id: Long = 1,
    pos: LatLon = p(),
    tags: Map<String, String>? = null,
    version: Int = 1,
    date: Date? = null
) = OsmNode(id, version, pos, tags, null, date)

fun way(
    id: Long = 1,
    nodes: List<Long> = listOf(),
    tags: Map<String, String>? = null,
    version: Int = 1,
    date: Date? = null
) = OsmWay(id, version, nodes, tags, null, date)

fun rel(
    id: Long = 1,
    members: List<RelationMember> = listOf(),
    tags: Map<String, String>? = null,
    version: Int = 1,
    date: Date? = null
) = OsmRelation(id, version, members, tags, null, date)

fun member(
    type: Element.Type = Element.Type.NODE,
    ref: Long = 1,
    role: String = ""
) = OsmRelationMember(ref, role, type)

fun bbox(latMin: Double = 0.0, lonMin: Double = 0.0, latMax: Double = 1.0, lonMax: Double = 1.0) =
    BoundingBox(latMin, lonMin, latMax, lonMax)

fun waysAsMembers(wayIds: List<Long>, role: String = ""): List<RelationMember> =
    wayIds.map { id -> member(Element.Type.WAY, id, role) }.toMutableList()

fun pGeom(lat: Double = 0.0, lon: Double = 0.0) = ElementPointGeometry(p(lat, lon))

fun note(
    id: Long = 1,
    position: LatLon = p(0.0, 0.0),
    timestamp: Long = 0,
    comments: List<NoteComment> = listOf(comment("test", NoteComment.Action.OPENED))
) = Note().also {
    it.id = id
    it.comments = comments
    it.dateCreated = Date(timestamp)
    it.position = position
    it.status = Note.Status.OPEN
}

fun comment(
    text: String,
    action: NoteComment.Action = NoteComment.Action.COMMENTED,
    timestamp: Long = 0,
    userId: Long? = null,
    userName: String? = null
) = NoteComment().also {
    it.text = text
    it.action = action
    it.user = userId?.let { User(userId, userName) }
    it.date = Date(timestamp)
}
