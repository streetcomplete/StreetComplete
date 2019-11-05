package de.westnordost.streetcomplete.data.osm.persist

object OsmQuestTable {
    const val NAME = "osm_quests"
    const val NAME_MERGED_VIEW = "osm_quests_full"

    object Columns {
        const val QUEST_ID = "quest_id"
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val QUEST_STATUS = "quest_status"
        const val TAG_CHANGES = "tag_changes"
        const val CHANGES_SOURCE = "changes_source"
        const val LAST_UPDATE = "last_update"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.QUEST_STATUS} varchar(255) NOT NULL,
            ${Columns.TAG_CHANGES} blob,
            ${Columns.CHANGES_SOURCE} varchar(255),
            ${Columns.LAST_UPDATE} int NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            CONSTRAINT same_osm_quest UNIQUE (
                ${Columns.QUEST_TYPE},
                ${Columns.ELEMENT_ID},
                ${Columns.ELEMENT_TYPE}
            ),
            CONSTRAINT element_key FOREIGN KEY (
                ${Columns.ELEMENT_TYPE},
                ${Columns.ELEMENT_ID}
            ) REFERENCES ${ElementGeometryTable.NAME} (
                ${ElementGeometryTable.Columns.ELEMENT_TYPE},
                ${ElementGeometryTable.Columns.ELEMENT_ID}
            )
        );"""

    const val CREATE_VIEW = """
        CREATE VIEW $NAME_MERGED_VIEW
        AS SELECT * FROM $NAME
        INNER JOIN ${ElementGeometryTable.NAME}
        USING (
            ${ElementGeometryTable.Columns.ELEMENT_TYPE},
            ${ElementGeometryTable.Columns.ELEMENT_ID}
        );"""

    val ALL_COLUMNS_DB_VERSION_3 = listOf(
        Columns.QUEST_ID,
        Columns.QUEST_TYPE,
        Columns.ELEMENT_ID,
        Columns.ELEMENT_TYPE,
        Columns.QUEST_STATUS,
        Columns.TAG_CHANGES,
        Columns.LAST_UPDATE
    )

    const val CREATE_DB_VERSION_3 = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.QUEST_STATUS} varchar(255) NOT NULL,
            ${Columns.TAG_CHANGES} blob,
            ${Columns.LAST_UPDATE} int NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            CONSTRAINT same_osm_quest UNIQUE (
                ${Columns.QUEST_TYPE},
                ${Columns.ELEMENT_ID},
                ${Columns.ELEMENT_TYPE}
            ),
            CONSTRAINT element_key FOREIGN KEY (
                ${Columns.ELEMENT_TYPE}, ${Columns.ELEMENT_ID}
            ) REFERENCES ${ElementGeometryTable.NAME} (
                ${ElementGeometryTable.Columns.ELEMENT_TYPE},
                ${ElementGeometryTable.Columns.ELEMENT_ID}
            )
        );"""
}
