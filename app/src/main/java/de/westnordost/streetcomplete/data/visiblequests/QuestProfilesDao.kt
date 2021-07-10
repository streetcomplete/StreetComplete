package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.visiblequests.QuestProfilesTable.NAME
import de.westnordost.streetcomplete.data.visiblequests.QuestProfilesTable.Columns.QUEST_PROFILE_ID
import de.westnordost.streetcomplete.data.visiblequests.QuestProfilesTable.Columns.QUEST_PROFILE_NAME
import javax.inject.Inject

class QuestProfilesDao @Inject constructor(private val db: Database) {

    fun add(name: String): Long =
        db.insert(NAME, listOf(QUEST_PROFILE_NAME to name))

    fun delete(id: Long) {
        db.delete(NAME, "$QUEST_PROFILE_ID = $id")
    }

    fun getAll(): List<QuestProfile> =
        db.query(NAME, orderBy = "$QUEST_PROFILE_ID ASC") {
            QuestProfile(it.getLong(QUEST_PROFILE_ID), it.getString(QUEST_PROFILE_NAME))
        }
}

data class QuestProfile(val id: Long, val name: String)
