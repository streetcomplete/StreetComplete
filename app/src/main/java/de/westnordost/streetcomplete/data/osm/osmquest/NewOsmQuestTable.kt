package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryTable

object NewOsmQuestTable {
    const val NAME = "osm_quests"
    const val NAME_MERGED_VIEW = "osm_quests_full"

    object Columns {
        const val QUEST_ID = "quest_id"
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
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

    const val MERGED_VIEW_CREATE = """
        CREATE VIEW $NAME_MERGED_VIEW
        AS SELECT * FROM $NAME
        INNER JOIN ${ElementGeometryTable.NAME}
        USING (
            ${ElementGeometryTable.Columns.ELEMENT_TYPE},
            ${ElementGeometryTable.Columns.ELEMENT_ID}
        );"""
}
