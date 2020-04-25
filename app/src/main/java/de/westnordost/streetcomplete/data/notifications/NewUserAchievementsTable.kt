package de.westnordost.streetcomplete.data.notifications

object NewUserAchievementsTable {
    const val NAME = "new_achievements"

    object Columns {
        const val ACHIEVEMENT = "achievement"
        const val LEVEL = "level"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ACHIEVEMENT} varchar(255),
            ${Columns.LEVEL} int NOT NULL,
            CONSTRAINT primary_key PRIMARY KEY (${Columns.ACHIEVEMENT}, ${Columns.LEVEL})
        );"""
}
