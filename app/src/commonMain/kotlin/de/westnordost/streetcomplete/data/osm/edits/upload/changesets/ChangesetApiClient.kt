package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException

interface ChangesetApiClient {
    /**
     * Open a new changeset with the given tags
     *
     * @param tags tags of this changeset. Usually it is comment and source.
     *
     * @throws AuthorizationException if the application does not have permission to edit the map
     *                                (OAuth scope "write_api")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the id of the changeset
     */
    suspend fun open(tags: Map<String, String>): Long

    /**
     * Closes the given changeset.
     *
     * @param id id of the changeset to close
     *
     * @throws ConflictException if the changeset has already been closed or does not exist
     * @throws AuthorizationException if the application does not have permission to edit the map
     *                                (OAuth scope "write_api")
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun close(id: Long)
}

