package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.common.errors.OsmApiReadResponseException
import de.westnordost.osmapi.common.errors.OsmAuthorizationException
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmConnectionException
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.download.QueryTooBigException
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.User
import de.westnordost.osmapi.map.data.BoundingBox as OsmApiBoundingBox
import de.westnordost.osmapi.notes.Note as OsmApiNote
import de.westnordost.osmapi.notes.NoteComment as OsmApiNoteComment
import de.westnordost.osmapi.notes.NotesApi as OsmApiNotesApi
import de.westnordost.osmapi.user.User as OsmApiUser

class NotesApiImpl(osm: OsmConnection) : NotesApi {
    private val api: OsmApiNotesApi = OsmApiNotesApi(osm)

    override fun create(pos: LatLon, text: String): Note = wrapExceptions {
        api.create(OsmLatLon(pos.latitude, pos.longitude), text).toNote()
    }

    override fun comment(id: Long, text: String): Note =
        try {
            wrapExceptions { api.comment(id, text).toNote() }
        } catch (e: OsmNotFoundException) {
            // someone else already closed the note -> our contribution is probably worthless
            throw ConflictException(e.message, e)
        }

    override fun get(id: Long): Note? = wrapExceptions { api.get(id)?.toNote() }

    override fun getAll(bounds: BoundingBox, limit: Int, hideClosedNoteAfter: Int) = wrapExceptions {
        val notes = ArrayList<Note>()
        api.getAll(
            OsmApiBoundingBox(
                bounds.min.latitude, bounds.min.longitude,
                bounds.max.latitude, bounds.max.longitude
            ),
            null,
            { notes.add(it.toNote()) },
            limit,
            hideClosedNoteAfter
        )
        notes
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

private fun OsmApiNote.toNote() = Note(
    LatLon(position.latitude, position.longitude),
    id,
    createdAt.toEpochMilli(),
    closedAt?.toEpochMilli(),
    status.toNoteStatus(),
    comments.map { it.toNoteComment() }
)

private fun OsmApiNote.Status.toNoteStatus() = when (this) {
    OsmApiNote.Status.OPEN   -> Note.Status.OPEN
    OsmApiNote.Status.CLOSED -> Note.Status.CLOSED
    OsmApiNote.Status.HIDDEN -> Note.Status.HIDDEN
    else -> throw NoSuchFieldError()
}

private fun OsmApiNoteComment.toNoteComment() = NoteComment(
    date.toEpochMilli(),
    action.toNoteCommentAction(),
    text,
    user?.toUser()
)

private fun OsmApiNoteComment.Action.toNoteCommentAction() = when (this) {
    OsmApiNoteComment.Action.OPENED     -> NoteComment.Action.OPENED
    OsmApiNoteComment.Action.COMMENTED  -> NoteComment.Action.COMMENTED
    OsmApiNoteComment.Action.CLOSED     -> NoteComment.Action.CLOSED
    OsmApiNoteComment.Action.REOPENED   -> NoteComment.Action.REOPENED
    OsmApiNoteComment.Action.HIDDEN     -> NoteComment.Action.HIDDEN
    else -> throw NoSuchFieldError()
}

private fun OsmApiUser.toUser() = User(id, displayName)
