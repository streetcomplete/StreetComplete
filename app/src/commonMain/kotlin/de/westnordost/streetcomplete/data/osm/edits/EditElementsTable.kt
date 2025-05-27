package de.westnordost.streetcomplete.data.osm.edits

object EditElementsTable {
    const val NAME = "osm_edit_elements"

    object Columns {
        const val EDIT_ID = "edit_id"
        const val ELEMENT_TYPE = "element_type"
        const val ELEMENT_ID = "element_id"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.EDIT_ID} INTEGER NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} text NOT NULL,
            CONSTRAINT same_osm_quest PRIMARY KEY (
                ${Columns.EDIT_ID},
                ${Columns.ELEMENT_TYPE},
                ${Columns.ELEMENT_ID}
            )
        );
    """

    const val INDEX_CREATE = """
        CREATE INDEX osm_edit_elements_index ON $NAME (
            ${Columns.ELEMENT_TYPE},
            ${Columns.ELEMENT_ID}
        );
    """
}
