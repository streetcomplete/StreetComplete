package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.CENTER_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.CENTER_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.GEOMETRY_POLYGONS
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.GEOMETRY_POLYLINES
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.MAX_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.MAX_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.MIN_LATITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.Columns.MIN_LONGITUDE
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.NAME_RELATIONS
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable.NAME_WAYS
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.NodeDao

/** Stores the geometry of elements */
class ElementGeometryDao(
    private val db: Database,
    private val polylinesSerializer: PolylinesSerializer,
    private val nodeDao: NodeDao
) {
    fun put(entry: ElementGeometryEntry) {
        when (entry.elementType) {
            ElementType.NODE -> Unit
            else -> db.replace(dbName(entry.elementType), entry.toPairs())
        }
    }

    fun get(type: ElementType, id: Long): ElementGeometry? =
        when (type) {
            ElementType.NODE -> nodeDao.get(id)?.let { ElementPointGeometry(it.position) }
            else -> db.queryOne(dbName(type),
                where = "$ID = $id",
                columns = arrayOf(ID, GEOMETRY_POLYGONS, GEOMETRY_POLYLINES, CENTER_LATITUDE, CENTER_LONGITUDE)
            ) { it.toElementGeometry() }
        }

    fun delete(type: ElementType, id: Long): Boolean =
        when (type) {
            ElementType.NODE -> false
            else -> db.delete(
                dbName(type),
                where = "$ID = $id"
            ) == 1
        }

    fun putAll(entries: Collection<ElementGeometryEntry>) {
        db.transaction {
            putAll(NAME_WAYS, entries.filter { it.elementType == ElementType.WAY })
            putAll(NAME_RELATIONS, entries.filter { it.elementType == ElementType.RELATION })
        }
    }

    fun putAll(table: String, entries: Collection<ElementGeometryEntry>) {
        if (entries.isEmpty()) return
        db.replaceMany(
            table,
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

    fun getAllKeys(bbox: BoundingBox): List<ElementKey> {
        val results = mutableListOf<ElementKey>()
        db.transaction {
            db.query(
                NAME_RELATIONS,
                columns = arrayOf(ID),
                where = inBoundsSql(bbox)
            ) { results.add(ElementKey(ElementType.RELATION, it.getLong(ID))) }
            db.query(
                NAME_WAYS,
                columns = arrayOf(ID),
                where = inBoundsSql(bbox)
            ) { results.add(ElementKey(ElementType.WAY, it.getLong(ID))) }
            results.addAll(nodeDao.getAllIds(bbox).map { ElementKey(ElementType.NODE, it) })
        }
        return results
    }


    fun getAllEntries(bbox: BoundingBox): List<ElementGeometryEntry> =
        getAllEntriesWithoutNodes(bbox) + nodeDao.getAllEntries(bbox)

    fun getAllEntriesWithoutNodes(bbox: BoundingBox): List<ElementGeometryEntry> {
        val results = mutableListOf<ElementGeometryEntry>()
        db.transaction {
            db.query(
                NAME_RELATIONS, where = inBoundsSql(bbox),
                columns = arrayOf(ID, GEOMETRY_POLYGONS, GEOMETRY_POLYLINES, CENTER_LATITUDE, CENTER_LONGITUDE)
            ) { results.add(ElementGeometryEntry(ElementType.RELATION, it.getLong(ID), it.toElementGeometry())) }
            db.query(
                NAME_WAYS, where = inBoundsSql(bbox),
                columns = arrayOf(ID, GEOMETRY_POLYGONS, GEOMETRY_POLYLINES, CENTER_LATITUDE, CENTER_LONGITUDE)
            ) { results.add(ElementGeometryEntry(ElementType.WAY, it.getLong(ID), it.toElementGeometry())) }
        }
        return results
    }

    fun getAllEntries(keys: Collection<ElementKey>): List<ElementGeometryEntry> {
        val results = mutableListOf<ElementGeometryEntry>()
        db.transaction {
            for (type in ElementType.values()) {
                results.addAll(
                    getAllEntriesForType(
                        type,
                        keys.mapNotNull { if (it.type == type) it.id else null }
                    )
                )
            }
        }
        return results
    }

    private fun getAllEntriesForType(type: ElementType, ids: List<Long>): List<ElementGeometryEntry> {
        if (ids.isEmpty()) return emptyList()
        return when (type) {
            ElementType.NODE -> nodeDao.getAllEntries(ids)
            else -> db.query(
                dbName(type),
                where = "$ID in (${ids.joinToString(",")})",
                columns = arrayOf(ID, GEOMETRY_POLYGONS, GEOMETRY_POLYLINES, CENTER_LATITUDE, CENTER_LONGITUDE)
            ) { ElementGeometryEntry(ElementType.WAY, it.getLong(ID), it.toElementGeometry()) }
        }
    }

    fun deleteAll(entries: Collection<ElementKey>): Int {
        if (entries.isEmpty()) return 0
        var deletedCount = 0
        db.transaction {
            for (entry in entries) {
                if (delete(entry.type, entry.id)) deletedCount++
            }
        }
        return deletedCount
    }

    fun clear() {
        db.delete(NAME_RELATIONS)
        db.delete(NAME_WAYS)
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

private fun dbName(type: ElementType) = when(type) {
    ElementType.WAY -> NAME_WAYS
    ElementType.RELATION -> NAME_RELATIONS
    ElementType.NODE -> throw(IllegalArgumentException("no geometry table for nodes"))
}

data class ElementGeometryEntry(
    val elementType: ElementType,
    val elementId: Long,
    val geometry: ElementGeometry
)

private typealias PolyLines = List<List<LatLon>>
