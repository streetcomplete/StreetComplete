package de.westnordost.streetcomplete.data.user.achievements

import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsTable.Columns.ACHIEVEMENT
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsTable.Columns.LEVEL
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsTable.NAME

import javax.inject.Inject

import de.westnordost.streetcomplete.ktx.*

/** Stores which achievement ids have been unlocked by the user and at which level */
class UserAchievementsDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    fun getAll(): Map<String, Int> {
        return db.query(NAME) {
            it.getString(ACHIEVEMENT) to it.getInt(LEVEL)
        }.toMap()
    }

    fun clear() {
        db.delete(NAME, null, null)
    }

    fun put(achievement: String, level: Int) {
        db.insertWithOnConflict(NAME, null, contentValuesOf(
            ACHIEVEMENT to achievement,
            LEVEL to level
        ), CONFLICT_REPLACE)
    }
}
