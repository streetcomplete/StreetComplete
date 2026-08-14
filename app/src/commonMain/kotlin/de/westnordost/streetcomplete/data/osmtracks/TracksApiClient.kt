package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException

/**
 * Talks with OSM traces API to uploads GPS trackpoints
 */
interface TracksApiClient {
    /**
     * Upload a list of trackpoints as a GPX
     *
     * @param trackpoints recorded trackpoints
     * @param creator user agent string
     * @param description optional description text
     * @param tags optional tags for the trace
     *
     * @throws AuthorizationException if not logged in or not not authorized to upload traces
     *                                (scope "write_gpx")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return id of the uploaded track
     */
    suspend fun create(
        trackpoints: List<Trackpoint>,
        creator: String,
        description: String? = null,
        tags: Iterable<String>? = null,
    ): Long
}
