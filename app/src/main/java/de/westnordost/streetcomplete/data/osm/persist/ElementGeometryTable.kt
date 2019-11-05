package de.westnordost.streetcomplete.data.osm.persist

object ElementGeometryTable {
    const val NAME = "elements_geometry"

    object Columns {
        const val ELEMENT_ID = OsmQuestTable.Columns.ELEMENT_ID
        const val ELEMENT_TYPE = OsmQuestTable.Columns.ELEMENT_TYPE
        const val GEOMETRY_POLYGONS = "geometry_polygons"
        const val GEOMETRY_POLYLINES = "geometry_polylines"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.GEOMETRY_POLYLINES} blob,
            ${Columns.GEOMETRY_POLYGONS} blob,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            CONSTRAINT primary_key PRIMARY KEY (
                ${Columns.ELEMENT_TYPE},
                ${Columns.ELEMENT_ID}
            )
        );"""
}
