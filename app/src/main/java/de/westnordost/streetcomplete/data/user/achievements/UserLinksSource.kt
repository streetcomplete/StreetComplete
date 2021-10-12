package de.westnordost.streetcomplete.data.user.achievements

interface UserLinksSource {
    /** Get the user's unlocked links */
    fun getLinks(): List<Link>
}
