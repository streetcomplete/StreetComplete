package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable.NAME
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import javax.inject.Inject

class CreatedElementsDao @Inject constructor(private val db: Database) {

    fun putAll(entries: Collection<ElementKey>) {
        if (entries.isEmpty()) return

        db.replaceMany(
            NAME,
            arrayOf(ELEMENT_TYPE, ELEMENT_ID),
            entries.map { arrayOf(it.type.name, it.id) }
        )
    }

    fun getAll(): List<ElementKey> =
        db.query(NAME) { it.toElementKey() }

    fun deleteAll(entries: Collection<ElementKey>) {
        if (entries.isEmpty()) return
        db.transaction {
            for (entry in entries) {
                db.delete(
                    NAME,
                    where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
                    args = arrayOf(entry.type.name, entry.id)
                )
            }
        }
    }
}

private fun CursorPosition.toElementKey() = ElementKey(
    ElementType.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID)
)
