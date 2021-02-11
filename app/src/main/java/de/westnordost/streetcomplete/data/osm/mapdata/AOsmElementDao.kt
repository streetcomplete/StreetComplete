package de.westnordost.streetcomplete.data.osm.mapdata

import android.database.sqlite.SQLiteOpenHelper

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.changes.OsmElementChangesTable
import de.westnordost.streetcomplete.ktx.query
import de.westnordost.streetcomplete.ktx.queryOne
import de.westnordost.streetcomplete.ktx.transaction

/** Abstract base class for the DAOs that store the OSM elements */
abstract class AOsmElementDao<T : Element>(private val dbHelper: SQLiteOpenHelper) {

    private val db get() = dbHelper.writableDatabase

    protected abstract val elementTypeName: String
    protected abstract val tableName: String
    protected abstract val idColumnName: String
    protected abstract val lastUpdateColumnName: String
    protected abstract val mapping: ObjectRelationalMapping<T>

    fun put(element: T) {
        db.replaceOrThrow(tableName, null, mapping.toContentValues(element))
    }

    fun get(id: Long): T? {
        return db.queryOne(tableName, null, "$idColumnName = $id", null) { mapping.toObject(it) }
    }

    fun delete(id: Long) {
        db.delete(tableName, "$idColumnName = $id", null)
    }

    fun putAll(elements: Collection<T>) {
        if (elements.isEmpty()) return
        db.transaction {
            for (element in elements) {
                put(element)
            }
        }
    }

    fun getAll(ids: Collection<Long>): List<T> {
        if (ids.isEmpty()) return emptyList()
        return db.query(tableName, null, "$idColumnName IN (${ids.joinToString(",")})") { mapping.toObject(it) }
    }

    fun deleteAll(ids: Collection<Long>) {
        if (ids.isEmpty()) return
        db.delete(tableName, "$idColumnName IN (${ids.joinToString(",")})", null)
    }

    /** Get all elements that are not referenced by any quest (or other things) anymore and older
     *  than the given timestamp*/
    open fun getUnusedAndOldIds(timestamp: Long): List<Long> {
        return db.query(tableName, arrayOf(idColumnName), """
            $lastUpdateColumnName < $timestamp AND
            $idColumnName NOT IN (
                SELECT ${OsmElementChangesTable.Columns.ELEMENT_ID} AS $idColumnName
                FROM ${OsmElementChangesTable.NAME}
                WHERE ${OsmElementChangesTable.Columns.ELEMENT_TYPE} = "$elementTypeName"
            )""".trimIndent()) {
            it.getLong(0)
        }
    }
}


