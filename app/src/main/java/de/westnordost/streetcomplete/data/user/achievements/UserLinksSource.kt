package de.westnordost.streetcomplete.data.user.achievements

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Provides the user's unlocked links */
@Singleton class UserLinksSource @Inject constructor(
    private val userLinksDao: UserLinksDao,
    @Named("Links") allLinks: List<Link>
) {
    private val linksById = allLinks.associateBy { it.id }

    fun getLinks(): List<Link> {
        return userLinksDao.getAll().mapNotNull { linksById[it] }
    }
}