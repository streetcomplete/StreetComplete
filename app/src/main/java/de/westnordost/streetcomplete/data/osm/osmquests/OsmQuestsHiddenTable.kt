package de.westnordost.streetcomplete.data.osm.osmquests

object OsmQuestsHiddenTable {
    const val NAME = "osm_quests_hidden"

    object Columns {
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val TIMESTAMP = "timestamp"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.TIMESTAMP} int NOT NULL,
            CONSTRAINT same_osm_quest PRIMARY KEY (
                ${Columns.QUEST_TYPE},
                ${Columns.ELEMENT_ID},
                ${Columns.ELEMENT_TYPE}
            )
        );"""
}
