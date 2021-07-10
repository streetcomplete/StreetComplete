package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable.Columns.AFTER
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable.Columns.BEFORE
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable.Columns.QUEST_PROFILE_ID
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable.NAME
import javax.inject.Inject

class QuestTypeOrderDao @Inject constructor(private val db: Database) {

    fun getAll(profileId: Long): List<Pair<String, String>> =
        db.query(NAME,
            where = "$QUEST_PROFILE_ID = $profileId",
            orderBy = "ROWID ASC"
        ) { cursor ->
            cursor.getString(BEFORE) to cursor.getString(AFTER)
        }

    fun put(profileId: Long, pair: Pair<String, String>) {
        db.insert(NAME, listOf(
            QUEST_PROFILE_ID to profileId,
            BEFORE to pair.first,
            AFTER to pair.second
        ))
    }

    fun clear(profileId: Long) {
        db.delete(NAME, where = "$QUEST_PROFILE_ID = $profileId")
    }
}
