package de.westnordost.streetcomplete.data.osm.osmquests

object OsmQuestTable {
    const val NAME = "osm_quests"

    object Columns {
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            PRIMARY KEY (
                ${Columns.ELEMENT_TYPE},
                ${Columns.ELEMENT_ID},
                ${Columns.QUEST_TYPE}
            )
        );
    """

    const val SPATIAL_INDEX_CREATE = """
        CREATE INDEX osm_quests_spatial_index ON $NAME (
            ${Columns.LATITUDE},
            ${Columns.LONGITUDE}
        );
    """

    const val CREATE_ELEMENT_ID_INDEX_IF_NOT_EXISTS = """
        CREATE INDEX IF NOT EXISTS osm_quests_id_index ON $NAME (
            ${Columns.ELEMENT_ID}
        );
    """
}
