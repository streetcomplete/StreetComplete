package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.user.achievements.UserLinksTable.Columns.LINK
import de.westnordost.streetcomplete.data.user.achievements.UserLinksTable.NAME

/** Stores which link ids have been unlocked by the user */
class UserLinksDao(private val db: Database) {

    fun getAll(): List<String> =
        db.query(NAME) { it.getString(LINK) }

    fun clear() {
        db.delete(NAME)
    }

    fun add(link: String) {
        db.insertOrIgnore(NAME, listOf(LINK to link))
    }

    fun addAll(links: List<String>) {
        if (links.isEmpty()) return
        db.insertOrIgnoreMany(NAME,
            arrayOf(LINK),
            links.map { arrayOf(it) }
        )
    }
}
