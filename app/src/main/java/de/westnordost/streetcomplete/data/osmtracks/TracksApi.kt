package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException

/**
 * Creates GPS / GPX trackpoint histories
 */
interface TracksApi {

    /**
     * Create a new GPX track history
     *
     * @param trackpoints history of recorded trackpoints
     * @param noteText optional text appended to the track
     *
     * @throws AuthorizationException if this application is not authorized to write traces
     *                                (Permission.WRITE_GPS_TRACES)
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @throws IllegalArgumentException if noteText is longer than 255 characters
     *
     * @return id of the new track
     */
    fun create(trackpoints: List<Trackpoint>, noteText: String?): Long
}
