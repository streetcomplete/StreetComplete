package de.westnordost.streetcomplete.data.osm.edits

object ElementIdProviderTable {
    const val NAME = "osm_element_id_provider"

    object Columns {
        const val ID = "id"
        const val EDIT_ID = "edit_id"
        const val ELEMENT_TYPE = "element_type"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Columns.EDIT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL
        );
        CREATE INDEX osm_element_id_provider_edit_id_idx ON $NAME (
            ${Columns.EDIT_ID}
        );
    """

    const val INDEX_CREATE = """
        CREATE INDEX osm_element_id_provider_edit_id_idx ON $NAME (
            ${Columns.EDIT_ID}
        );
    """
}
