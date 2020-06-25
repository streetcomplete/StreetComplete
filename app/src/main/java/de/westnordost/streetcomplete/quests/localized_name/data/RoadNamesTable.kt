package de.westnordost.streetcomplete.quests.localized_name.data

object RoadNamesTable {
    const val NAME = "road_names"

    object Columns {
        const val WAY_ID = "way_id"
        const val NAMES = "names"
        const val GEOMETRY = "geometry"
        const val MIN_LATITUDE = "min_latitude"
        const val MIN_LONGITUDE = "min_longitude"
        const val MAX_LATITUDE = "max_latitude"
        const val MAX_LONGITUDE = "max_longitude"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.WAY_ID} int PRIMARY KEY,
            ${Columns.NAMES} blob NOT NULL,
            ${Columns.GEOMETRY} blob NOT NULL,
            ${Columns.MIN_LATITUDE} double NOT NULL,
            ${Columns.MIN_LONGITUDE} double NOT NULL,
            ${Columns.MAX_LATITUDE} double NOT NULL,
            ${Columns.MAX_LONGITUDE} double NOT NULL
        );"""
}
