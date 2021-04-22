package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.NAME
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import java.lang.System.currentTimeMillis
import javax.inject.Inject

/** Persists which osm quests should be hidden (because the user selected so) */
class OsmQuestsHiddenDao @Inject constructor(private val db: Database) {

    fun add(osmQuestKey: OsmQuestKey) {
        db.insert(NAME, osmQuestKey.toPairs())
    }

    fun contains(osmQuestKey: OsmQuestKey): Boolean =
        getTimestamp(osmQuestKey) != null

    fun getTimestamp(osmQuestKey: OsmQuestKey): Long? =
        db.queryOne(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(
                osmQuestKey.elementType.name,
                osmQuestKey.elementId,
                osmQuestKey.questTypeName
            )
        ) { it.getLong(TIMESTAMP) }

    fun delete(osmQuestKey: OsmQuestKey): Boolean =
        db.delete(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            args = arrayOf(
                osmQuestKey.elementType.name,
                osmQuestKey.elementId,
                osmQuestKey.questTypeName
            )
        ) == 1

    fun getNewerThan(timestamp: Long): List<OsmQuestKeyWithTimestamp> =
        db.query(NAME, where = "$TIMESTAMP > $timestamp") { it.toHiddenOsmQuest() }

    fun getAllIds(): List<OsmQuestKey> =
        db.query(NAME) { it.toOsmQuestKey() }

    fun deleteAll(): Int =
        db.delete(NAME)
}

private fun OsmQuestKey.toPairs() = listOf(
    ELEMENT_TYPE to elementType.name,
    ELEMENT_ID to elementId,
    QUEST_TYPE to questTypeName,
    TIMESTAMP to currentTimeMillis()
)

private fun CursorPosition.toOsmQuestKey() = OsmQuestKey(
    Element.Type.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID),
    getString(QUEST_TYPE)
)

private fun CursorPosition.toHiddenOsmQuest() = OsmQuestKeyWithTimestamp(toOsmQuestKey(), getLong(TIMESTAMP))

data class OsmQuestKeyWithTimestamp(val osmQuestKey: OsmQuestKey, val timestamp: Long)
