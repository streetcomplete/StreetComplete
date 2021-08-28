package de.westnordost.streetcomplete.data.osm.geometry

import javax.inject.Inject

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
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.queryIn

/** Stores the geometry of elements */
class ElementGeometryDao @Inject constructor(
    private val db: Database,
    private val polylinesSerializer: PolylinesSerializer
) {
    fun put(entry: ElementGeometryEntry) {
        db.replace(NAME, entry.toPairs())
    }

    fun get(type: ElementType, id: Long): ElementGeometry? =
        db.queryOne(NAME,
            where = "$ELEMENT_TYPE = ? AND $ELEMENT_ID = ?",
            args = arrayOf(type.name, id)
        ) { it.toElementGeometry() }

    fun delete(type: ElementType, id: Long): Boolean =
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
                    if (g is ElementPolygonsGeometry) polylinesSerializer.serialize(g.polygons) else null,
                    if (g is ElementPolylinesGeometry) polylinesSerializer.serialize(g.polylines) else null,
                    bbox.min.latitude,
                    bbox.min.longitude,
                    bbox.max.latitude,
                    bbox.max.longitude
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
        return db.queryIn(NAME,
            whereColumns = arrayOf(ELEMENT_TYPE, ELEMENT_ID),
            whereArgs = keys.map { arrayOf(it.type.name, it.id) }
        ) { it.toElementGeometryEntry() }
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
        ElementType.valueOf(getString(ELEMENT_TYPE)),
        getLong(ELEMENT_ID),
        toElementGeometry()
    )

    private fun ElementGeometry.toPairs() = listOf(
        CENTER_LATITUDE to center.latitude,
        CENTER_LONGITUDE to center.longitude,
        GEOMETRY_POLYGONS to if (this is ElementPolygonsGeometry) polylinesSerializer.serialize(polygons) else null,
        GEOMETRY_POLYLINES to if (this is ElementPolylinesGeometry) polylinesSerializer.serialize(polylines) else null,
        MIN_LATITUDE to getBounds().min.latitude,
        MIN_LONGITUDE to getBounds().min.longitude,
        MAX_LATITUDE to getBounds().max.latitude,
        MAX_LONGITUDE to getBounds().max.longitude
    )

    private fun CursorPosition.toElementGeometry(): ElementGeometry {
        val polylines: PolyLines? = getBlobOrNull(GEOMETRY_POLYLINES)?.let { polylinesSerializer.deserialize(it) }
        val polygons: PolyLines? = getBlobOrNull(GEOMETRY_POLYGONS)?.let { polylinesSerializer.deserialize(it) }
        val center = LatLon(getDouble(CENTER_LATITUDE), getDouble(CENTER_LONGITUDE))

        return when {
            polygons != null -> ElementPolygonsGeometry(polygons, center)
            polylines != null -> ElementPolylinesGeometry(polylines, center)
            else -> ElementPointGeometry(center)
        }
    }
}

private fun inBoundsSql(bbox: BoundingBox) = """
    $MIN_LATITUDE <= ${bbox.max.latitude} AND
    $MAX_LATITUDE >= ${bbox.min.latitude} AND
    $MIN_LONGITUDE <= ${bbox.max.longitude} AND
    $MAX_LONGITUDE >= ${bbox.min.longitude}
""".trimIndent()

private fun CursorPosition.toElementKey() = ElementKey(
    ElementType.valueOf(getString(ELEMENT_TYPE)),
    getLong(ELEMENT_ID)
)

data class ElementGeometryEntry(
    val elementType: ElementType,
    val elementId: Long,
    val geometry: ElementGeometry
)

private typealias PolyLines = List<List<LatLon>>
