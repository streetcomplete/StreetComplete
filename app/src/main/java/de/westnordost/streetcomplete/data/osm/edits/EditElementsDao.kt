package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.edits.EditElementsTable.Columns.EDIT_ID
import de.westnordost.streetcomplete.data.osm.edits.EditElementsTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.edits.EditElementsTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.edits.EditElementsTable.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

class EditElementsDao(private val db: Database) {

    fun put(id: Long, elementKeys: List<ElementKey>) {
        db.insertMany(
            NAME,
            arrayOf(EDIT_ID, ELEMENT_TYPE, ELEMENT_ID),
            elementKeys.map { arrayOf(id, it.type.name, it.id) }
        )
    }

    fun get(id: Long): List<ElementKey> =
        db.query(NAME, where = "$EDIT_ID = $id") {
            ElementKey(ElementType.valueOf(it.getString(ELEMENT_TYPE)), it.getLong(ELEMENT_ID))
        }

    fun getAllByElement(elementType: ElementType, elementId: Long): List<Long> =
        db.query(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            args = arrayOf(elementType.name, elementId),
            columns = arrayOf(EDIT_ID)
        ) { it.getLong(EDIT_ID) }

    fun delete(id: Long): Int =
        db.delete(NAME, "$EDIT_ID = $id")

    fun deleteAll(ids: List<Long>): Int {
        if (ids.isEmpty()) return 0
        return db.delete(NAME, "$EDIT_ID in (${ids.joinToString(",")})" )
    }
}
