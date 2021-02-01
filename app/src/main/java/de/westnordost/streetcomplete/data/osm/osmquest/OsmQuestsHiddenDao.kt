package de.westnordost.streetcomplete.data.osm.osmquest

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestsHiddenTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestsHiddenTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestsHiddenTable.Columns.QUEST_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestsHiddenTable.NAME
import de.westnordost.streetcomplete.ktx.getLong
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.queryOne
import de.westnordost.streetcomplete.ktx.query
import javax.inject.Inject

/** Persists which osm quests should be hidden (because the user selected so) */
class OsmQuestsHiddenDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: OsmQuestsHiddenMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun add(osmQuestKey: OsmQuestKey) {
        db.insert(NAME, null, mapping.toContentValues(osmQuestKey))
    }

    fun contains(osmQuestKey: OsmQuestKey): Boolean =
        db.queryOne(NAME, null,
            "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ? AND $QUEST_TYPE = ?",
            arrayOf(
                osmQuestKey.elementType.toString(),
                osmQuestKey.elementId.toString(),
                osmQuestKey.questTypeName)
        ) { true } ?: false

    fun getAll(): List<OsmQuestKey> =
        db.query(NAME) { mapping.toObject(it) }

    fun deleteAll(): Int =
        db.delete(NAME, null, null)
}

class OsmQuestsHiddenMapping @Inject constructor() : ObjectRelationalMapping<OsmQuestKey> {
    override fun toContentValues(obj: OsmQuestKey) = contentValuesOf(
        ELEMENT_TYPE to obj.elementType.name,
        ELEMENT_ID to obj.elementId,
        QUEST_TYPE to obj.questTypeName
    )

    override fun toObject(cursor: Cursor) = OsmQuestKey(
        Element.Type.valueOf(cursor.getString(ELEMENT_TYPE)),
        cursor.getLong(ELEMENT_ID),
        cursor.getString(QUEST_TYPE)
    )
}
