package de.westnordost.streetcomplete.testutils

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmRelation
import de.westnordost.osmapi.map.data.OsmRelationMember
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.RelationMember
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.toOsmLatLon
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.user.User
import java.util.*

fun p(lat: Double = 0.0, lon: Double = 0.0) = LatLon(lat, lon)

fun node(
    id: Long = 1,
    pos: LatLon = p(),
    tags: Map<String, String>? = null,
    version: Int = 1,
    date: Date? = null
) = OsmNode(id, version, pos.toOsmLatLon(), tags, null, date)

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
) = Note(position, id, timestamp, null, Note.Status.OPEN, comments)

fun comment(
    text: String,
    action: NoteComment.Action = NoteComment.Action.COMMENTED,
    timestamp: Long = 0,
    userId: Long? = null,
    userName: String? = null
) = NoteComment(timestamp, action, text, User.from(userId, userName))

fun noteEdit(
    id: Long = 1,
    noteId: Long = 5,
    action: NoteEditAction = NoteEditAction.COMMENT,
    text: String = "test123",
    timestamp: Long = 123L,
    imagePaths: List<String> = emptyList(),
    pos: LatLon = p(1.0, 1.0),
    isSynced: Boolean = false
) = NoteEdit(
    id,
    noteId,
    pos,
    action,
    text,
    imagePaths,
    timestamp,
    isSynced,
    imagePaths.isNotEmpty()
)

fun edit(
    id: Long = 1L,
    elementType: Element.Type = Element.Type.NODE,
    elementId: Long = -1L,
    pos: LatLon = p(0.0,0.0),
    timestamp: Long = 123L,
    action: ElementEditAction = DeletePoiNodeAction(1),
    isSynced: Boolean = false
) = ElementEdit(
    id,
    QUEST_TYPE,
    elementType,
    elementId,
    "survey",
    pos,
    timestamp,
    isSynced,
    action
)

fun questHidden(
    elementType: Element.Type = Element.Type.NODE,
    elementId: Long = 1L,
    questType: OsmElementQuestType<*> = QUEST_TYPE,
    pos: LatLon = p(),
    timestamp: Long = 123L
) = OsmQuestHidden(elementType, elementId, questType, pos, timestamp)

fun noteQuestHidden(
    note: Note = note(),
    timestamp: Long = 123L
) = OsmNoteQuestHidden(note, timestamp)

fun osmQuest(
    questType: OsmElementQuestType<*> = QUEST_TYPE,
    elementType: Element.Type = Element.Type.NODE,
    elementId: Long = 1L,
    geometry: ElementGeometry = pGeom()
) =
    OsmQuest(questType, elementType, elementId, geometry)

fun osmNoteQuest(
    id: Long = 1L,
    pos: LatLon = p()
) = OsmNoteQuest(id, pos)

fun osmQuestKey(
    elementType: Element.Type = Element.Type.NODE,
    elementId: Long = 1L,
    questTypeName: String = QUEST_TYPE::class.simpleName!!
) = OsmQuestKey(elementType, elementId, questTypeName)

val QUEST_TYPE = TestQuestTypeA()
