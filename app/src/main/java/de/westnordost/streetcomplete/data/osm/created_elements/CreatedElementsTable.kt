package de.westnordost.streetcomplete.data.osm.created_elements

object CreatedElementsTable {
    const val NAME = "created_elements"

    object Columns {
        const val ELEMENT_ID = "element_id"
        const val NEW_ELEMENT_ID = "new_element_id"
        const val ELEMENT_TYPE = "element_type"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.NEW_ELEMENT_ID} int,
            CONSTRAINT primary_key PRIMARY KEY (
                ${Columns.ELEMENT_TYPE},
                ${Columns.ELEMENT_ID}
            )
        );
    """

    const val NEW_ID_INDEX_CREATE = """
        CREATE INDEX created_elements_new_ids_idx ON $NAME (
            ${Columns.ELEMENT_TYPE},
            ${Columns.NEW_ELEMENT_ID}
        );
    """
}
