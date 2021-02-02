package de.westnordost.streetcomplete.data.osm.osmquest.changes

object OsmElementTagChangesTable {
    const val NAME = "element_tag_changes"

    object Columns {
        const val ID = "id"
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val TAG_CHANGES = "tag_changes"
        const val CHANGES_SOURCE = "changes_source"
        const val IS_REVERT = "is_revert"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.TAG_CHANGES} blob NOT NULL,
            ${Columns.CHANGES_SOURCE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.IS_REVERT} BOOLEAN NOT NULL,
            CONSTRAINT same_osm_quest UNIQUE (
                ${Columns.QUEST_TYPE},
                ${Columns.ELEMENT_ID},
                ${Columns.ELEMENT_TYPE}
            )
        );"""
}
