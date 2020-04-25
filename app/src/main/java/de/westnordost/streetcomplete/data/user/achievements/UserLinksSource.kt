package de.westnordost.streetcomplete.data.user.achievements

import javax.inject.Inject
import javax.inject.Named

/** Provides the user's unlocked links */
class UserLinksSource @Inject constructor(
    private val userLinksDao: UserLinksDao,
    @Named("Links") allLinks: List<Link>
) {
    private val linksById = allLinks.associateBy { it.id }

    fun getLinks(): List<Link> {
        return userLinksDao.getAll().mapNotNull { linksById[it] }
    }
}