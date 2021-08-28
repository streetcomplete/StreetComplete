package de.westnordost.streetcomplete.data.osm.geometry

object ElementGeometryTable {
    const val NAME = "elements_geometry"

    object Columns {
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val GEOMETRY_POLYGONS = "geometry_polygons"
        const val GEOMETRY_POLYLINES = "geometry_polylines"
        const val CENTER_LATITUDE = "latitude"
        const val CENTER_LONGITUDE = "longitude"
        const val MIN_LATITUDE = "min_lat"
        const val MIN_LONGITUDE = "min_lon"
        const val MAX_LATITUDE = "max_lat"
        const val MAX_LONGITUDE = "max_lon"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.GEOMETRY_POLYLINES} blob,
            ${Columns.GEOMETRY_POLYGONS} blob,
            ${Columns.CENTER_LATITUDE} double NOT NULL,
            ${Columns.CENTER_LONGITUDE} double NOT NULL,
            ${Columns.MIN_LATITUDE} double NOT NULL,
            ${Columns.MAX_LATITUDE} double NOT NULL,
            ${Columns.MIN_LONGITUDE} double NOT NULL,
            ${Columns.MAX_LONGITUDE} double NOT NULL,
            CONSTRAINT primary_key PRIMARY KEY (
                ${Columns.ELEMENT_TYPE},
                ${Columns.ELEMENT_ID}
            )
        );"""

    const val SPATIAL_INDEX_CREATE = """
        CREATE INDEX elements_geometry_bounds_index ON $NAME (
            ${Columns.MIN_LATITUDE},
            ${Columns.MAX_LATITUDE},
            ${Columns.MIN_LONGITUDE},
            ${Columns.MAX_LONGITUDE}
        );
    """
}

