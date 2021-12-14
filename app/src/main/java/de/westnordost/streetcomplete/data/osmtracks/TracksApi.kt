package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.download.ConnectionException
import de.westnordost.streetcomplete.data.user.AuthorizationException

/**
 * Creates GPS / GPX trackpoint histories
 * All interactions with this class require an OsmConnection with a logged in user.
 */
interface TracksApi {

    /**
     * Create a new GPX track history
     *
     * @param tracks history of recorded trackpoints
     *
     * @throws AuthorizationException if this application is not authorized to write notes
     *                                (Permission.READ_GPS_TRACES, Permission.WRITE_GPS_TRACES)
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the new track
     */
    fun create(tracks: List<Trackpoint>): Track

}
