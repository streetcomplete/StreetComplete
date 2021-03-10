package de.westnordost.streetcomplete.data.osm.osmquests

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable.NAME
import de.westnordost.streetcomplete.ktx.getLong
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.queryOne
import de.westnordost.streetcomplete.ktx.query
import java.lang.System.currentTimeMillis
import javax.inject.Inject

/** Persists which osm quests should be hidden (because the user selected so) */
class OsmQuestsHiddenDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    fun add(osmQuestKey: OsmQuestKey) {
        db.insert(NAME, null, osmQuestKey.toContentValues())
    }

    fun contains(osmQuestKey: OsmQuestKey): Boolean =
        db.queryOne(NAME,
            selection = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            selectionArgs = arrayOf(
                osmQuestKey.elementType.toString(),
                osmQuestKey.elementId.toString(),
                osmQuestKey.questTypeName
            )
        ) { true } ?: false

    fun getAll(): List<OsmQuestKey> =
        db.query(NAME) { it.toOsmQuestKey() }

    fun deleteAll(): Int =
        db.delete(NAME, null, null)
}

private fun OsmQuestKey.toContentValues() = contentValuesOf(
    ELEMENT_TYPE to elementType.name,
    ELEMENT_ID to elementId,
    QUEST_TYPE to questTypeName,
    TIMESTAMP to currentTimeMillis()
)

private fun Cursor.toOsmQuestKey() = OsmQuestKey(
    Element.Type.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID),
    getString(QUEST_TYPE)
)
