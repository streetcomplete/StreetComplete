package de.westnordost.streetcomplete.data.osm.geometry

object WayGeometryTable {
    const val NAME = "elements_geometry_ways"

    object Columns {
        const val ID = "id"
        const val GEOMETRY_POLYGONS = "geometry_polygons"
        const val GEOMETRY_POLYLINES = "geometry_polylines"
        const val CENTER_LATITUDE = "latitude"
        const val CENTER_LONGITUDE = "longitude"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.GEOMETRY_POLYLINES} blob,
            ${Columns.GEOMETRY_POLYGONS} blob,
            ${Columns.CENTER_LATITUDE} double NOT NULL,
            ${Columns.CENTER_LONGITUDE} double NOT NULL
        );
    """
}
