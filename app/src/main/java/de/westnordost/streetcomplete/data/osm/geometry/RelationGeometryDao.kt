package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.CENTER_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.CENTER_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.GEOMETRY_POLYGONS
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.GEOMETRY_POLYLINES
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.MAX_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.MAX_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.MIN_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.Columns.MIN_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/** Stores the geometry of relations */
class RelationGeometryDao(
    private val db: Database,
    private val polylinesSerializer: PolylinesSerializer,
) {
    fun put(entry: ElementGeometryEntry) {
        if (entry.elementType != ElementType.RELATION)
            throw(IllegalArgumentException("trying to store ${entry.elementType.name} geometry in relation geometry table"))
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
        if (entries.any { it.elementType != ElementType.RELATION })
            throw(IllegalArgumentException("trying to store non-relation geometry in relation geometry table"))
        db.replaceMany(NAME,
            arrayOf(
                ID,
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
                    it.elementId,
                    g.center.latitude,
                    g.center.longitude,
                    if (g is ElementPolygonsGeometry) polylinesSerializer.serialize(g.polygons) else null,
                    if (g is ElementPolylinesGeometry) polylinesSerializer.serialize(g.polylines) else null,
                    bbox.min.latitude,
                    bbox.min.longitude,
                    bbox.max.latitude,
                    bbox.max.longitude
                )
            }
        )
    }

    fun getAllKeys(bbox: BoundingBox): List<ElementKey> =
        db.query(NAME,
            columns = arrayOf(ID),
            where = inBoundsSql(bbox)
        ) { ElementKey(ElementType.RELATION, it.getLong(ID)) }

    fun getAllEntries(bbox: BoundingBox): List<ElementGeometryEntry> =
        db.query(NAME,
            where = inBoundsSql(bbox),
            columns = arrayOf(ID, GEOMETRY_POLYGONS, GEOMETRY_POLYLINES, CENTER_LATITUDE, CENTER_LONGITUDE)
        ) { it.toElementGeometryEntry() }

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
        GEOMETRY_POLYLINES to if (this is ElementPolylinesGeometry) polylinesSerializer.serialize(polylines) else null,
        MIN_LATITUDE to getBounds().min.latitude,
        MIN_LONGITUDE to getBounds().min.longitude,
        MAX_LATITUDE to getBounds().max.latitude,
        MAX_LONGITUDE to getBounds().max.longitude
    )

    private fun CursorPosition.toElementGeometryEntry(): ElementGeometryEntry =
        ElementGeometryEntry(
            ElementType.RELATION,
            getLong(ID),
            toElementGeometry()
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
