package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.user.achievements.NewUserAchievementsTable.Columns.ACHIEVEMENT
import de.westnordost.streetcomplete.data.user.achievements.NewUserAchievementsTable.Columns.LEVEL
import de.westnordost.streetcomplete.data.user.achievements.NewUserAchievementsTable.NAME
import javax.inject.Inject

/** Stores which achievements have *newly* been unlocked by the user and which levels. */
class NewUserAchievementsDao @Inject constructor(private val db: Database) {

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
            }
            result = r
        }
        return result
    }

    fun getCount(): Int =
        db.queryOne(NAME, arrayOf("COUNT(*) AS count")) { it.getInt("count") } ?: 0

    fun push(achievementAndLevel: Pair<String, Int>): Boolean {
        return db.insertOrIgnore(NAME, listOf(
            ACHIEVEMENT to achievementAndLevel.first,
            LEVEL to achievementAndLevel.second
        )) != -1L
    }

    fun clear() {
        db.delete(NAME)
    }
}
