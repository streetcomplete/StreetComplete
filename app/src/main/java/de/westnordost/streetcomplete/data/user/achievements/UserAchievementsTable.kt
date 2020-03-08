package de.westnordost.streetcomplete.data.user.achievements

object UserAchievementsTable {
    const val NAME = "achievements"

    object Columns {
        const val ACHIEVEMENT = "achievement"
        const val LEVEL = "level"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ACHIEVEMENT} varchar(255) PRIMARY KEY,
            ${Columns.LEVEL} int NOT NULL
        );"""
}
