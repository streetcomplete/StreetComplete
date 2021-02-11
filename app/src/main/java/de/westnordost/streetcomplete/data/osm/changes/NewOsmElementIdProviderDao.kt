package de.westnordost.streetcomplete.data.osm.changes

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.changes.NewOsmElementIdProviderTable.Columns.CHANGE_ID
import de.westnordost.streetcomplete.data.osm.changes.NewOsmElementIdProviderTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.changes.NewOsmElementIdProviderTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.changes.NewOsmElementIdProviderTable.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.ktx.getLong
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.query
import de.westnordost.streetcomplete.ktx.transaction
import javax.inject.Inject

/** Assigns new element ids for OsmElementChanges that create new elements */
class NewOsmElementIdProviderDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper
) {
    private val db get() = dbHelper.writableDatabase

    fun assign(changeId: Long, nodeCount: Int, wayCount: Int, relationCount: Int) {
        db.transaction {
            repeat(nodeCount) { assign(changeId, Element.Type.NODE) }
            repeat(wayCount) { assign(changeId, Element.Type.WAY) }
            repeat(relationCount) { assign(changeId, Element.Type.RELATION) }
        }
    }

    private fun assign(changeId: Long, elementType: Element.Type) {
        db.insert(NAME, null, contentValuesOf(
            CHANGE_ID to changeId,
            ELEMENT_TYPE to elementType.name
        ))
    }

    fun get(changeId: Long): NewOsmElementIdProvider? {
        val elementKeys = db.query(NAME, selection = "$CHANGE_ID = $changeId") {
            ElementKey(Element.Type.valueOf(it.getString(ELEMENT_TYPE)), -it.getLong(ID))
        }
        return if (elementKeys.isEmpty()) null else NewOsmElementIdProvider(elementKeys)
    }

    fun delete(changeId: Long): Int =
        db.delete(NAME, "$CHANGE_ID = $changeId", null)
}
