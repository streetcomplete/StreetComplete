package de.westnordost.streetcomplete.data.osm.changes

object NewOsmElementIdProviderTable {
    const val NAME = "osm_element_id_provider"

    object Columns {
        const val ID = "id"
        const val CHANGE_ID = "change_id"
        const val ELEMENT_TYPE = "element_type"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.CHANGE_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL
        );
        CREATE INDEX osm_element_id_provider_element_type_idx ON $NAME (
            ${Columns.CHANGE_ID}
        );"""
}
