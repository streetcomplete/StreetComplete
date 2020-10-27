package de.westnordost.streetcomplete.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryTable

@Entity(
    tableName = ElementGeometryTable.NAME,
    primaryKeys = [
        ElementGeometryTable.Columns.ELEMENT_TYPE,
        ElementGeometryTable.Columns.ELEMENT_ID
    ]
)
data class ElementGeometry(
    @ColumnInfo(name = ElementGeometryTable.Columns.ELEMENT_TYPE)
    val elementType: String,

    @ColumnInfo(name = ElementGeometryTable.Columns.ELEMENT_ID)
    val elementId: Int,

    @ColumnInfo(name = ElementGeometryTable.Columns.GEOMETRY_POLYGONS)
    val polygons: List<List<LatLon>>,

    @ColumnInfo(name = ElementGeometryTable.Columns.GEOMETRY_POLYLINES)
    val polylines: List<List<LatLon>>,

    @Embedded
    var latLon: LatLon
)
