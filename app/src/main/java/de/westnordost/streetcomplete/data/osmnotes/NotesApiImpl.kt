package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.NotesApi as OsmapiNotesApi
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.data.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import de.westnordost.osmapi.map.data.BoundingBox as OsmApiBoundingBox
import de.westnordost.osmapi.map.data.LatLon as OsmApiLatLon
import de.westnordost.osmapi.notes.Note as OsmApiNote
import de.westnordost.osmapi.notes.NoteComment as OsmApiNoteComment
import de.westnordost.osmapi.user.User as OsmApiUser

// TODO(Flo): Make parameter non-nullable
open class NotesApiImpl(osm: OsmConnection?) : NotesApi {
    private val api: OsmapiNotesApi = OsmapiNotesApi(osm)

    override fun create(pos: LatLon, text: String): Note =
        api.create(pos.toOsmLatLon(), text).toNote()

    override fun comment(id: Long, text: String): Note = api.comment(id, text).toNote()

    override fun get(id: Long): Note? = api.get(id)?.toNote()

    override suspend fun getAll(bounds: BoundingBox, limit: Int, hideClosedNoteAfter: Int) =
        withContext(Dispatchers.IO) {
            val notes = ArrayList<Note>()
            api.getAll(bounds.toOsmApiBoundingBox(), null, { notes.add(it.toNote()) },
                limit, hideClosedNoteAfter)
            notes
        }
}

private fun OsmApiNote.toNote() = Note(
    position.toLatLon(),
    id,
    createdAt.toEpochMilli(),
    closedAt?.toEpochMilli(),
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
    date.toEpochMilli(),
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

// TODO(Flo): make this private
fun LatLon.toOsmLatLon() = OsmLatLon(latitude, longitude)

// TODO(Flo): make this private
fun OsmApiLatLon.toLatLon() = LatLon(latitude, longitude)

// TODO(Flo): make this private
fun BoundingBox.toOsmApiBoundingBox() =
    OsmApiBoundingBox(min.latitude, min.longitude, max.latitude, max.longitude)

// TODO(Flo): make this private
fun OsmApiBoundingBox.toBoundingBox() =
    BoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude)
