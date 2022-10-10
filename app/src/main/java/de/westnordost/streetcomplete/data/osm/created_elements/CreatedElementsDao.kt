package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable.Columns.NEW_ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType

/** Persists which elements have been created, each including their initial (local/temporary) id
 *  before upload and the assigned (if any, yet) id by the OSM API */
class CreatedElementsDao(private val db: Database) {

    fun putAll(entries: Collection<CreatedElementKey>) {
        if (entries.isEmpty()) return

        db.replaceMany(
            NAME,
            arrayOf(ELEMENT_TYPE, ELEMENT_ID, NEW_ELEMENT_ID),
            entries.map { arrayOf(it.elementType.name, it.elementId, it.newElementId) }
        )
    }

    fun getAll(): List<CreatedElementKey> =
        db.query(NAME) { it.toCreatedElementKey() }

    fun deleteAll(entries: Collection<ElementKey>) {
        if (entries.isEmpty()) return
        db.transaction {
            for (entry in entries) {
                db.delete(
                    NAME,
                    where = "$ELEMENT_TYPE = ? AND ($ELEMENT_ID = ? OR $NEW_ELEMENT_ID = ?)",
                    args = arrayOf(entry.type.name, entry.id)
                )
            }
        }
    }

    fun clear() {
        db.delete(NAME)
    }
}

private fun CursorPosition.toCreatedElementKey() = CreatedElementKey(
    ElementType.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID),
    getLongOrNull(NEW_ELEMENT_ID),
)
