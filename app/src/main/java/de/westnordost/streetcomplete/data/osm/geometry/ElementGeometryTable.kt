package de.westnordost.streetcomplete.data.osm.geometry

object ElementGeometryTable {
    const val NAME_RELATIONS = "elements_geometry_relations"
    const val NAME_WAYS = "elements_geometry_ways"

    object Columns {
        const val ID = "id"
        const val GEOMETRY_POLYGONS = "geometry_polygons"
        const val GEOMETRY_POLYLINES = "geometry_polylines"
        const val CENTER_LATITUDE = "latitude"
        const val CENTER_LONGITUDE = "longitude"
        const val MIN_LATITUDE = "min_lat"
        const val MIN_LONGITUDE = "min_lon"
        const val MAX_LATITUDE = "max_lat"
        const val MAX_LONGITUDE = "max_lon"
    }

    const val CREATE_WAYS = """
        CREATE TABLE $NAME_WAYS (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.GEOMETRY_POLYLINES} blob,
            ${Columns.GEOMETRY_POLYGONS} blob,
            ${Columns.CENTER_LATITUDE} double NOT NULL,
            ${Columns.CENTER_LONGITUDE} double NOT NULL,
            ${Columns.MIN_LATITUDE} double NOT NULL,
            ${Columns.MAX_LATITUDE} double NOT NULL,
            ${Columns.MIN_LONGITUDE} double NOT NULL,
            ${Columns.MAX_LONGITUDE} double NOT NULL
        );"""

    const val CREATE_RELATIONS = """
        CREATE TABLE $NAME_RELATIONS (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.GEOMETRY_POLYLINES} blob,
            ${Columns.GEOMETRY_POLYGONS} blob,
            ${Columns.CENTER_LATITUDE} double NOT NULL,
            ${Columns.CENTER_LONGITUDE} double NOT NULL,
            ${Columns.MIN_LATITUDE} double NOT NULL,
            ${Columns.MAX_LATITUDE} double NOT NULL,
            ${Columns.MIN_LONGITUDE} double NOT NULL,
            ${Columns.MAX_LONGITUDE} double NOT NULL
        );"""

    const val SPATIAL_INDEX_CREATE_WAYS = """
        CREATE INDEX elements_geometry_bounds_index_ways ON $NAME_WAYS (
            ${Columns.MIN_LATITUDE},
            ${Columns.MAX_LATITUDE},
            ${Columns.MIN_LONGITUDE},
            ${Columns.MAX_LONGITUDE}
        );
    """

    const val SPATIAL_INDEX_CREATE_RELATIONS = """
        CREATE INDEX elements_geometry_bounds_index_relations ON $NAME_RELATIONS (
            ${Columns.MIN_LATITUDE},
            ${Columns.MAX_LATITUDE},
            ${Columns.MIN_LONGITUDE},
            ${Columns.MAX_LONGITUDE}
        );
    """
}
