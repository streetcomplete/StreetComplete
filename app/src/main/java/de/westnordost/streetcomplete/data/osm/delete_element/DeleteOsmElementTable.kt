package de.westnordost.streetcomplete.data.osm.delete_element

object DeleteOsmElementTable {
    const val NAME = "osm_delete_elements"

    object Columns {
        const val QUEST_ID = "quest_id"
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val SOURCE = "source"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_ID} int PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} blob NOT NULL,
            ${Columns.SOURCE} varchar(255) NOT NULL,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL
        );"""
}
