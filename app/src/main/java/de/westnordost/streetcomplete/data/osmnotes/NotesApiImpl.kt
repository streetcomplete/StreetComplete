package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.notes.Note as OsmApiNote
import de.westnordost.osmapi.notes.NoteComment as OsmApiNoteComment
import de.westnordost.osmapi.notes.NotesDao
import de.westnordost.osmapi.user.User as OsmApiUser
import de.westnordost.osmapi.map.data.BoundingBox as OsmApiBoundingBox
import de.westnordost.osmapi.map.data.LatLon as OsmApiLatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.data.user.User


class NotesApiImpl(osm: OsmConnection) : NotesApi {
    private val notesDao: NotesDao = NotesDao(osm)

    override fun create(pos: LatLon, text: String): Note =
        notesDao.create(pos.toOsmLatLon(), text).toNote()

    override fun comment(id: Long, text: String): Note = notesDao.comment(id, text).toNote()

    override fun get(id: Long): Note? = notesDao.get(id)?.toNote()

    override fun getAll(bounds: BoundingBox, handler: (Note) -> Unit,
                        limit: Int, hideClosedNoteAfter: Int) =
        notesDao.getAll(bounds.toOsmApiBoundingBox(), null, { handler(it.toNote()) },
            limit, hideClosedNoteAfter)
}

private fun OsmApiNote.toNote() = Note(
    position.toLatLon(),
    id,
    dateCreated.time,
    dateClosed?.time,
    status.toNoteStatus(),
    comments.map { it.toNoteComment() }
)

private fun OsmApiNote.Status.toNoteStatus() = when(this) {
    OsmApiNote.Status.OPEN   -> Note.Status.OPEN
    OsmApiNote.Status.CLOSED -> Note.Status.CLOSED
    OsmApiNote.Status.HIDDEN -> Note.Status.HIDDEN
    else -> throw NoSuchFieldError()
}

private fun OsmApiNoteComment.toNoteComment() = NoteComment(
    date.time,
    action.toNoteCommentAction(),
    text,
    user.toUser()
)

private fun OsmApiNoteComment.Action.toNoteCommentAction() = when(this) {
    OsmApiNoteComment.Action.OPENED     -> NoteComment.Action.OPENED
    OsmApiNoteComment.Action.COMMENTED  -> NoteComment.Action.COMMENTED
    OsmApiNoteComment.Action.CLOSED     -> NoteComment.Action.CLOSED
    OsmApiNoteComment.Action.REOPENED   -> NoteComment.Action.REOPENED
    OsmApiNoteComment.Action.HIDDEN     -> NoteComment.Action.HIDDEN
    else -> throw NoSuchFieldError()
}

private fun OsmApiUser.toUser() = User(id, displayName)

private fun LatLon.toOsmLatLon() = OsmLatLon(latitude, longitude)

private fun OsmApiLatLon.toLatLon() = LatLon(latitude, longitude)

private fun BoundingBox.toOsmApiBoundingBox() =
    OsmApiBoundingBox(min.latitude, min.longitude, max.latitude, max.longitude)
