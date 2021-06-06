package de.westnordost.streetcomplete.data.osm.created_elements

object CreatedElementsTable {
    const val NAME = "created_elements"

    object Columns {
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            CONSTRAINT primary_key PRIMARY KEY (
                ${Columns.ELEMENT_TYPE},
                ${Columns.ELEMENT_ID}
            )
        );"""
}

