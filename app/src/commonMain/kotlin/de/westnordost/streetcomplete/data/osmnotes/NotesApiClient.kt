package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.QueryTooBigException
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/**
 * Creates, comments, closes, reopens and search for notes.
 */
interface NotesApiClient {
    /**
     * Create a new note at the given location
     *
     * @param pos position of the note.
     * @param text text for the new note. Must not be empty.
     *
     * @throws AuthorizationException if this application is not authorized to write notes
     *                                (scope "write_notes")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the new note
     */
    suspend fun create(pos: LatLon, text: String): Note

    /**
     * @param id id of the note
     * @param text comment to be added to the note. Must not be empty
     *
     * @throws ConflictException if the note has already been closed or doesn't exist (anymore).
     * @throws AuthorizationException if this application is not authorized to write notes
     *                                (scope "write_notes")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the updated commented note
     */
    suspend fun comment(id: Long, text: String): Note

    /**
     * @param id id of the note
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the note with the given id. null if the note with that id does not exist (anymore).
     */
    suspend fun get(id: Long): Note?

    /**
     * Retrieve all open notes in the given area
     *
     * @param bounds within this area notes will be queried. This is usually limited at 25
     *               square degrees. Check the server capabilities.
     * @param limit number of entries returned at maximum. Any value between 1 and 10000
     *
     * @throws QueryTooBigException if the bounds area or the limit is too large
     * @throws IllegalArgumentException if the bounds cross the 180th meridian
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the incoming notes
     */
    suspend fun getAllOpen(bounds: BoundingBox, limit: Int? = null): List<Note>
}
