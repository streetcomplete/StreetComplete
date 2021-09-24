package de.westnordost.streetcomplete.data.notifications

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.notifications.NewUserAchievementsTable.Columns.ACHIEVEMENT
import de.westnordost.streetcomplete.data.notifications.NewUserAchievementsTable.Columns.LEVEL
import de.westnordost.streetcomplete.data.notifications.NewUserAchievementsTable.NAME
import javax.inject.Inject

import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

/** Stores which achievements have *newly* been unlocked by the user and which levels. */
@Singleton class NewUserAchievementsDao @Inject constructor(private val db: Database) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    interface Listener {
        fun onNewUserAchievementsUpdated()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun pop(): Pair<String, Int>? {
        var result: Pair<String, Int>? = null
        db.transaction {
            val r = db.queryOne(NAME, orderBy = "$ACHIEVEMENT, $LEVEL ASC") {
                it.getString(ACHIEVEMENT) to it.getInt(LEVEL)
            }
            if (r != null) {
                db.delete(NAME,
                    where = "$ACHIEVEMENT = ? AND $LEVEL = ?",
                    args = arrayOf(r.first, r.second)
                )
                onNewUserAchievementsChanged()
            }
            result = r
        }
        return result
    }

    fun getCount(): Int =
        db.queryOne(NAME, arrayOf("COUNT(*) AS count")) { it.getInt("count") } ?: 0

    fun push(achievementAndLevel: Pair<String, Int>) {
        val result = db.insertOrIgnore(NAME, listOf(
            ACHIEVEMENT to achievementAndLevel.first,
            LEVEL to achievementAndLevel.second
        ))
        if (result != -1L) {
            onNewUserAchievementsChanged()
        }
    }

    fun clear() {
        db.delete(NAME)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun onNewUserAchievementsChanged() {
        for (listener in listeners) {
            listener.onNewUserAchievementsUpdated()
        }
    }
}
