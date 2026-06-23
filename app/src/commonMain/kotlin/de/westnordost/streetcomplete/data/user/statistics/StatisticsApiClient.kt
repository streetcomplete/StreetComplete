package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.ConnectionException

/** Client for the statistics service
 *  https://github.com/streetcomplete/sc-statistics-service/ */
interface StatisticsApiClient {
    /** Get the statistics for the given user id
     *
     * @throws ConnectionException on connection or server error */
    suspend fun get(osmUserId: Long): Statistics
}
