package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryTable.Columns.CENTER_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryTable.Columns.CENTER_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryTable.Columns.GEOMETRY_POLYGONS
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryTable.Columns.GEOMETRY_POLYLINES
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryTable.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/** Stores the geometry of ways */
class WayGeometryDao(
    private val db: Database,
    private val polylinesSerializer: PolylinesSerializer,
) {
    fun put(entry: ElementGeometryEntry) {
        if (entry.elementType != ElementType.WAY) {
            throw(IllegalArgumentException("trying to store ${entry.elementType.name} geometry in way geometry table"))
        }
        db.replace(NAME, entry.toPairs())
    }

    fun get(id: Long): ElementGeometry? =
        db.queryOne(NAME,
            where = "$ID = $id",
            columns = arrayOf(ID, GEOMETRY_POLYGONS, GEOMETRY_POLYLINES, CENTER_LATITUDE, CENTER_LONGITUDE)
        ) { it.toElementGeometry() }

    fun delete(id: Long): Boolean =
        db.delete(NAME, where = "$ID = $id") == 1

    fun putAll(entries: Collection<ElementGeometryEntry>) {
        if (entries.isEmpty()) return
        if (entries.any { it.elementType != ElementType.WAY }) {
            throw(IllegalArgumentException("trying to store non-way geometry in way geometry table"))
        }
        db.replaceMany(NAME,
            arrayOf(
                ID,
                CENTER_LATITUDE,
                CENTER_LONGITUDE,
                GEOMETRY_POLYGONS,
                GEOMETRY_POLYLINES
            ),
            entries.map {
                val g = it.geometry
                arrayOf(
                    it.elementId,
                    g.center.latitude,
                    g.center.longitude,
                    if (g is ElementPolygonsGeometry) polylinesSerializer.serialize(g.polygons) else null,
                    if (g is ElementPolylinesGeometry) polylinesSerializer.serialize(g.polylines) else null
                )
            }
        )
    }

    fun getAllEntries(ids: List<Long>): List<ElementGeometryEntry> {
        if (ids.isEmpty()) return emptyList()
        return db.query(NAME,
            where = "$ID in (${ids.joinToString(",")})",
            columns = arrayOf(ID, GEOMETRY_POLYGONS, GEOMETRY_POLYLINES, CENTER_LATITUDE, CENTER_LONGITUDE)
        ) { it.toElementGeometryEntry() }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        var deletedCount = 0
        db.transaction {
            for (id in ids) {
                if (delete(id)) deletedCount++
            }
        }
        return deletedCount
    }

    fun clear() {
        db.delete(NAME)
    }

    private fun ElementGeometryEntry.toPairs() = listOf(
        ID to elementId
    ) + geometry.toPairs()

    private fun ElementGeometry.toPairs() = listOf(
        CENTER_LATITUDE to center.latitude,
        CENTER_LONGITUDE to center.longitude,
        GEOMETRY_POLYGONS to if (this is ElementPolygonsGeometry) polylinesSerializer.serialize(polygons) else null,
        GEOMETRY_POLYLINES to if (this is ElementPolylinesGeometry) polylinesSerializer.serialize(polylines) else null
    )

    private fun CursorPosition.toElementGeometryEntry() = ElementGeometryEntry(
        ElementType.WAY,
        getLong(ID),
        toElementGeometry()
    )

    private fun CursorPosition.toElementGeometry(): ElementGeometry {
        val polylines: List<List<LatLon>>? = getBlobOrNull(GEOMETRY_POLYLINES)?.let { polylinesSerializer.deserialize(it) }
        val polygons: List<List<LatLon>>? = getBlobOrNull(GEOMETRY_POLYGONS)?.let { polylinesSerializer.deserialize(it) }
        val center = LatLon(getDouble(CENTER_LATITUDE), getDouble(CENTER_LONGITUDE))

        return when {
            polygons != null -> ElementPolygonsGeometry(polygons, center)
            polylines != null -> ElementPolylinesGeometry(polylines, center)
            else -> ElementPointGeometry(center)
        }
    }
}
