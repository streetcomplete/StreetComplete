package de.westnordost.streetcomplete.data.osm.persist

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf


import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryTable.Columns.GEOMETRY_POLYGONS
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryTable.Columns.GEOMETRY_POLYLINES
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryTable.NAME
import de.westnordost.streetcomplete.ktx.*

class ElementGeometryDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: ElementGeometryMapping
) {
    private val db get() = dbHelper.writableDatabase

    fun putAll(entries: Collection<ElementGeometryEntry>) {
        db.transaction {
            for (entry in entries) {
                put(entry)
            }
        }
    }

    fun put(entry: ElementGeometryEntry) {
        val values = contentValuesOf(
            ELEMENT_TYPE to entry.elementType.name,
            ELEMENT_ID to entry.elementId
        ) + mapping.toContentValues(entry.geometry)

        db.replaceOrThrow(NAME, null, values)
    }

    fun get(type: Element.Type, id: Long): ElementGeometry? {
        val where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?"
        val args = arrayOf(type.name, id.toString())

        return db.queryOne(NAME, null, where, args) { mapping.toObject(it) }
    }

    fun delete(type: Element.Type, id: Long) {
        val where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?"
        val args = arrayOf(type.name, id.toString())

        db.delete(NAME, where, args)
    }

    /** Cleans up element geometry entries that belong to elements that are not referenced by any
     * quest anymore.  */
    fun deleteUnreferenced(): Int {
        /* SQLite does not allow selecting multiple columns in a DELETE subquery. Using a workaround
         * as described here:
         * http://blog.programmingsolution.net/sql-server-2008/tsql/delete-rows-of-a-table-matching-multiple-columns-of-another-table/
         */
        val where = "(" + ELEMENT_TYPE + LUMP + ELEMENT_ID + ") NOT IN (" +
            "SELECT " + OsmQuestTable.Columns.ELEMENT_TYPE + LUMP + OsmQuestTable.Columns.ELEMENT_ID + " FROM " + OsmQuestTable.NAME + " " +
            "UNION SELECT " + UndoOsmQuestTable.Columns.ELEMENT_TYPE + LUMP + UndoOsmQuestTable.Columns.ELEMENT_ID + " FROM " + UndoOsmQuestTable.NAME +
            ")"

        return db.delete(NAME, where, null)
    }
}

data class ElementGeometryEntry(
    val elementType: Element.Type,
    val elementId: Long,
    val geometry: ElementGeometry
)

private const val LUMP = "+'#'+"
private typealias PolyLines = ArrayList<ArrayList<OsmLatLon>>

class ElementGeometryMapping @Inject constructor(
    private val serializer: Serializer)
    : ObjectRelationalMapping<ElementGeometry> {

    override fun toContentValues(obj: ElementGeometry) = contentValuesOf(
        LATITUDE to obj.center.latitude,
        LONGITUDE to obj.center.longitude,
        GEOMETRY_POLYGONS to (obj as? ElementPolygonsGeometry)?.let { serializer.toBytes(obj.polygons) },
        GEOMETRY_POLYLINES to (obj as? ElementPolylinesGeometry)?.let { serializer.toBytes(obj.polylines) }
    )

    override fun toObject(cursor: Cursor): ElementGeometry {
        val polylines = cursor.getBlobOrNull(GEOMETRY_POLYLINES)?.let { serializer.toObject<PolyLines>(it) }
        val polygons = cursor.getBlobOrNull(GEOMETRY_POLYGONS)?.let { serializer.toObject<PolyLines>(it) }
        val center = OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE))

        return when {
            polygons != null ->  ElementPolygonsGeometry(polygons, center)
            polylines != null -> ElementPolylinesGeometry(polylines, center)
            else ->              ElementPointGeometry(center)
        }
    }
}
