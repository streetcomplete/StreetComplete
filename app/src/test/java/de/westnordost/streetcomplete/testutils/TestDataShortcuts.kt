package de.westnordost.streetcomplete.testutils

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.delete.DeletePoiNodeAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.user.User
import java.lang.System.currentTimeMillis

fun p(lat: Double = 0.0, lon: Double = 0.0) = LatLon(lat, lon)

fun node(
    id: Long = 1,
    pos: LatLon = p(),
    tags: Map<String, String> = emptyMap(),
    version: Int = 1,
    timestamp: Long? = null
) = Node(id, pos, tags, version, timestamp ?: currentTimeMillis())

fun way(
    id: Long = 1,
    nodes: List<Long> = listOf(),
    tags: Map<String, String> = emptyMap(),
    version: Int = 1,
    timestamp: Long? = null
) = Way(id, nodes, tags, version, timestamp ?: currentTimeMillis())

fun rel(
    id: Long = 1,
    members: List<RelationMember> = listOf(),
    tags: Map<String, String> = emptyMap(),
    version: Int = 1,
    timestamp: Long? = null
) = Relation(id, members.toMutableList(), tags, version, timestamp ?: currentTimeMillis())

fun member(
    type: ElementType = ElementType.NODE,
    ref: Long = 1,
    role: String = ""
) = RelationMember(type, ref, role)

fun bbox(latMin: Double = 0.0, lonMin: Double = 0.0, latMax: Double = 1.0, lonMax: Double = 1.0) =
    BoundingBox(latMin, lonMin, latMax, lonMax)

fun waysAsMembers(wayIds: List<Long>, role: String = ""): List<RelationMember> =
    wayIds.map { id -> member(ElementType.WAY, id, role) }.toMutableList()

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
    user: User? = null
) = NoteComment(timestamp, action, text, user)

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
    element: Element = node(),
    geometry: ElementGeometry = pGeom(),
    timestamp: Long = 123L,
    action: ElementEditAction = DeletePoiNodeAction,
    isSynced: Boolean = false
) = ElementEdit(
    id,
    QUEST_TYPE,
    element.type,
    element.id,
    element,
    geometry,
    "survey",
    timestamp,
    isSynced,
    action
)

fun questHidden(
    elementType: ElementType = ElementType.NODE,
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
    elementType: ElementType = ElementType.NODE,
    elementId: Long = 1L,
    geometry: ElementGeometry = pGeom()
) =
    OsmQuest(questType, elementType, elementId, geometry)

fun osmNoteQuest(
    id: Long = 1L,
    pos: LatLon = p()
) = OsmNoteQuest(id, pos)

fun osmQuestKey(
    elementType: ElementType = ElementType.NODE,
    elementId: Long = 1L,
    questTypeName: String = QUEST_TYPE::class.simpleName!!
) = OsmQuestKey(elementType, elementId, questTypeName)

val QUEST_TYPE = TestQuestTypeA()
