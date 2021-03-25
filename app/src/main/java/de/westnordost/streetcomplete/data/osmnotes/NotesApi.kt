package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.common.errors.*
import de.westnordost.osmapi.notes.QueryNotesFilters
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon


/**
 * Creates, comments, closes, reopens and search for notes.
 * All interactions with this class require an OsmConnection with a logged in user.
 */
interface NotesApi {
    /**
     * Create a new note at the given location
     *
     * @param pos position of the note.
     * @param text text for the new note. Must not be empty.
     *
     * @throws OsmAuthorizationException if this application is not authorized to write notes
     *                                   (Permission.WRITE_NOTES)
     *
     * @return the new note
     */
    fun create(pos: LatLon, text: String): Note

    /**
     * @param id id of the note
     * @param text comment to be added to the note. Must not be empty
     *
     * @throws OsmConflictException if the note has already been closed.
     * @throws OsmAuthorizationException if this application is not authorized to write notes
     *                                   (Permission.WRITE_NOTES)
     * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
     *
     * @return the updated commented note
     */
    fun comment(id: Long, text: String): Note

    /**
     * Reopen the given note with the given reason. The reason is optional.
     *
     * @param id the id of the note to reopen
     * @param reason the reason why it is reopened.
     *
     * @throws OsmConflictException if the note has already been reopened.
     * @throws OsmAuthorizationException if this application is not authorized to write notes
     *                                   (Permission.WRITE_NOTES)
     * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
     *
     * @return the updated reopened note
     */
    fun reopen(id: Long, reason: String? = null): Note

    /**
     * Close aka resolve the note with the given id and reason.
     *
     * @param id id of the note
     * @param reason comment to be added to the note as a reason for it being closed. Optional.
     *
     * @throws OsmConflictException if the note has already been closed.
     * @throws OsmAuthorizationException if this application is not authorized to write notes
     *                                   (Permission.WRITE_NOTES)
     * @throws OsmNotFoundException if the note with the given id does not exist (anymore)
     *
     * @return the closed note
     */
    fun close(id: Long, reason: String? = null): Note

    /**
     * @param id id of the note
     *
     * @return the note with the given id. null if the note with that id does not exist (anymore).
     */
    fun get(id: Long): Note?

    /**
     * Retrieve all notes in the given area and feed them to the given handler.
     *
     * @see .getAll
     */
    fun getAll(bounds: BoundingBox, handler: Handler<Note>, limit: Int, hideClosedNoteAfter: Int) {
        getAll(bounds, null, handler, limit, hideClosedNoteAfter)
    }

    /**
     * Retrieve those notes in the given area that match the given search string
     *
     * @param bounds the area within the notes should be queried. This is usually limited at 25
     *               square degrees. Check the server capabilities.
     * @param search what to search for. Null to return everything.
     * @param handler The handler which is fed the incoming notes
     * @param limit number of entries returned at maximum. Any value between 1 and 10000
     * @param hideClosedNoteAfter number of days until a closed note should not be shown anymore.
     *                            -1 means that all notes should be returned, 0 that only open notes
     *                            are returned.
     *
     * @throws OsmQueryTooBigException if the bounds area is too large
     * @throws IllegalArgumentException if the bounds cross the 180th meridian
     */
    fun getAll(bounds: BoundingBox, search: String?, handler: Handler<Note>,
               limit: Int, hideClosedNoteAfter: Int)

    /**
     * Get a number of notes that match the given filters.
     *
     * @param handler The handler which is fed the incoming notes
     * @param filters what to search for. I.e. `QueryNotesFilters().byUser(123).limit(1000)`
     */
    fun find(handler: Handler<Note>, filters: QueryNotesFilters?)
}
