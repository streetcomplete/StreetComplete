package de.westnordost.streetcomplete.data.osm.edits

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProviderTable.Columns.EDIT_ID
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProviderTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProviderTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProviderTable.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.ktx.getLong
import de.westnordost.streetcomplete.ktx.getString
import de.westnordost.streetcomplete.ktx.query
import de.westnordost.streetcomplete.ktx.transaction
import javax.inject.Inject

/** Assigns new element ids for ElementEditActions that create new elements */
class ElementIdProviderDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper
) {
    private val db get() = dbHelper.writableDatabase

    fun assign(editId: Long, nodeCount: Int, wayCount: Int, relationCount: Int) {
        if (nodeCount == 0 && wayCount == 0 && relationCount == 0) return
        db.transaction {
            repeat(nodeCount) { assign(editId, Element.Type.NODE) }
            repeat(wayCount) { assign(editId, Element.Type.WAY) }
            repeat(relationCount) { assign(editId, Element.Type.RELATION) }
        }
    }

    private fun assign(editId: Long, elementType: Element.Type) {
        db.insert(NAME, null, contentValuesOf(
            EDIT_ID to editId,
            ELEMENT_TYPE to elementType.name
        ))
    }

    fun get(editId: Long): ElementIdProvider {
        val elementKeys = db.query(NAME, selection = "$EDIT_ID = $editId") {
            ElementKey(Element.Type.valueOf(it.getString(ELEMENT_TYPE)), -it.getLong(ID))
        }
        return ElementIdProvider(elementKeys)
    }

    fun delete(editId: Long): Int =
        db.delete(NAME, "$EDIT_ID = $editId", null)
}
