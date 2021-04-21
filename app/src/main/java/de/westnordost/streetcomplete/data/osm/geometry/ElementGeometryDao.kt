package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.osmapi.map.data.BoundingBox


import javax.inject.Inject

import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.GEOMETRY_POLYGONS
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.GEOMETRY_POLYLINES
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.CENTER_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.CENTER_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.MAX_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.MAX_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.MIN_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.MIN_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.NAME
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.NAME_TEMPORARY_LOOKUP
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.NAME_TEMPORARY_LOOKUP_MERGED_VIEW
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.TEMPORARY_LOOKUP_CREATE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.TEMPORARY_LOOKUP_MERGED_VIEW_CREATE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.ktx.*

/** Stores the geometry of elements */
class ElementGeometryDao @Inject constructor(
    private val db: Database,
    private val serializer: Serializer
) {
    fun put(entry: ElementGeometryEntry) {
        db.replace(NAME, entry.toPairs())
    }

    fun get(type: Element.Type, id: Long): ElementGeometry? =
        db.queryOne(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            args = arrayOf(type.name, id)
        ) { it.toElementGeometry() }

    fun delete(type: Element.Type, id: Long): Boolean =
        db.delete(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            args = arrayOf(type.name, id)
        ) == 1

    fun putAll(entries: Collection<ElementGeometryEntry>) {
        if (entries.isEmpty()) return

        db.replaceMany(NAME,
            arrayOf(
                ELEMENT_TYPE,
                ELEMENT_ID,
                CENTER_LATITUDE,
                CENTER_LONGITUDE,
                GEOMETRY_POLYGONS,
                GEOMETRY_POLYLINES,
                MIN_LATITUDE,
                MIN_LONGITUDE,
                MAX_LATITUDE,
                MAX_LONGITUDE
            ),
            entries.map {
                val bbox = it.geometry.getBounds()
                val g = it.geometry
                arrayOf(
                    it.elementType.name,
                    it.elementId,
                    g.center.latitude,
                    g.center.longitude,
                    if (g is ElementPolygonsGeometry) serializer.toBytes(g.polygons) else null,
                    if (g is ElementPolylinesGeometry) serializer.toBytes(g.polylines) else null,
                    bbox.minLatitude,
                    bbox.minLongitude,
                    bbox.maxLatitude,
                    bbox.maxLongitude
            ) }
        )
    }

    fun getAllKeys(bbox: BoundingBox): List<ElementKey> =
        db.query(NAME,
            columns = arrayOf(ELEMENT_TYPE, ELEMENT_ID),
            where = inBoundsSql(bbox)
        ) { it.toElementKey() }

    fun getAllEntries(bbox: BoundingBox): List<ElementGeometryEntry> =
        db.query(NAME, where = inBoundsSql(bbox)) { it.toElementGeometryEntry() }

    fun getAllEntries(keys: Collection<ElementKey>): List<ElementGeometryEntry> {
        if (keys.isEmpty()) return emptyList()
        return db.transaction {
            /* this looks a little complicated. Basically, this is a workaround for SQLite not
               supporting the "SELECT id FROM foo WHERE (a,b) IN ((1,2), (3,4), (5,6))" syntax:
               Instead, we insert the values into a temporary table and inner join on that table then
               https://stackoverflow.com/questions/18363276/how-do-you-do-an-in-query-that-has-multiple-columns-in-sqlite
             */
            db.exec(TEMPORARY_LOOKUP_CREATE)
            db.exec(TEMPORARY_LOOKUP_MERGED_VIEW_CREATE)
            db.insertOrIgnoreMany(NAME_TEMPORARY_LOOKUP,
                arrayOf(ELEMENT_TYPE, ELEMENT_ID),
                keys.map { arrayOf(it.type.name, it.id) }
            )
            val result = db.query(NAME_TEMPORARY_LOOKUP_MERGED_VIEW) { it.toElementGeometryEntry() }
            db.exec("DROP VIEW $NAME_TEMPORARY_LOOKUP_MERGED_VIEW")
            db.exec("DROP TABLE $NAME_TEMPORARY_LOOKUP")
            result
        }
    }

    fun deleteAll(entries: Collection<ElementKey>):Int {
        if (entries.isEmpty()) return 0
        var deletedCount = 0
        db.transaction {
            for (entry in entries) {
                if (delete(entry.type, entry.id)) deletedCount++
            }
        }
        return deletedCount
    }

    private fun ElementGeometryEntry.toPairs() = listOf(
        ELEMENT_TYPE to elementType.name,
        ELEMENT_ID to elementId
    ) + geometry.toPairs()

    private fun CursorPosition.toElementGeometryEntry() = ElementGeometryEntry(
        Element.Type.valueOf(getString(ELEMENT_TYPE)),
        getLong(ELEMENT_ID),
        toElementGeometry()
    )

    private fun ElementGeometry.toPairs() = listOf(
        CENTER_LATITUDE to center.latitude,
        CENTER_LONGITUDE to center.longitude,
        GEOMETRY_POLYGONS to if (this is ElementPolygonsGeometry) serializer.toBytes(polygons) else null,
        GEOMETRY_POLYLINES to if (this is ElementPolylinesGeometry) serializer.toBytes(polylines) else null,
        MIN_LATITUDE to getBounds().minLatitude,
        MIN_LONGITUDE to getBounds().minLongitude,
        MAX_LATITUDE to getBounds().maxLatitude,
        MAX_LONGITUDE to getBounds().maxLongitude
    )

    private fun CursorPosition.toElementGeometry(): ElementGeometry {
        val polylines = getBlobOrNull(GEOMETRY_POLYLINES)?.let { serializer.toObject<PolyLines>(it) }
        val polygons = getBlobOrNull(GEOMETRY_POLYGONS)?.let { serializer.toObject<PolyLines>(it) }
        val center = OsmLatLon(getDouble(CENTER_LATITUDE), getDouble(CENTER_LONGITUDE))

        return when {
            polygons != null -> ElementPolygonsGeometry(polygons, center)
            polylines != null -> ElementPolylinesGeometry(polylines, center)
            else -> ElementPointGeometry(center)
        }
    }
}

private fun inBoundsSql(bbox: BoundingBox) = """
    $MAX_LONGITUDE >= ${bbox.minLongitude} AND
    $MAX_LATITUDE >= ${bbox.minLatitude} AND
    $MIN_LONGITUDE <= ${bbox.maxLongitude} AND
    $MIN_LATITUDE <= ${bbox.maxLatitude}
""".trimIndent()

private fun CursorPosition.toElementKey() = ElementKey(
    Element.Type.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID)
)

data class ElementGeometryEntry(
    val elementType: Element.Type,
    val elementId: Long,
    val geometry: ElementGeometry
)

private typealias PolyLines = ArrayList<ArrayList<OsmLatLon>>
