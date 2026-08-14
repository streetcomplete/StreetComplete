package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException

/**
 * Talks with OSM user API
 */
interface UserApiClient {
    /**
     * @return the user info of the current user
     *
     * @throws AuthorizationException if we are not authorized to read user details (scope "read_prefs")
     * @throws ConnectionException on connection or server error
     */
    suspend fun getMine(): UserInfo

    /**
     * @param userId id of the user to get the user info for
     * @return the user info of the given user. Null if the user does not exist.
     *
     * @throws ConnectionException on connection or server error
     */
    suspend fun get(userId: Long): UserInfo?
}

