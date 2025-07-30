package de.westnordost.streetcomplete.data.osm.edits

object ElementIdProviderTable {
    const val NAME = "osm_element_id_provider"

    object Columns {
        const val ELEMENT_ID = "id"
        const val EDIT_ID = "edit_id"
        const val ELEMENT_TYPE = "element_type"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ELEMENT_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Columns.EDIT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL
        );
    """

    const val INDEX_CREATE = """
        CREATE INDEX osm_element_id_provider_edit_id_idx ON $NAME (
            ${Columns.EDIT_ID}
        );
    """

    const val ELEMENT_INDEX_CREATE = """
        CREATE INDEX osm_element_id_provider_element_idx ON $NAME (
            ${Columns.ELEMENT_TYPE},
            ${Columns.EDIT_ID}
        );
    """
}
