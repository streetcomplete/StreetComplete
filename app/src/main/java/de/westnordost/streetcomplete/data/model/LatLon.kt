package de.westnordost.streetcomplete.data.model

import androidx.room.ColumnInfo
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryTable

data class LatLon(
    @ColumnInfo(name = ElementGeometryTable.Columns.LATITUDE)
    var latitude: Double,
    @ColumnInfo(name = ElementGeometryTable.Columns.LONGITUDE)
    var longitude: Double
)
