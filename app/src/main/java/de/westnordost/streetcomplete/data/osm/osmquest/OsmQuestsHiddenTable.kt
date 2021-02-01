package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestsHiddenTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestsHiddenTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestsHiddenTable.Columns.QUEST_TYPE

object OsmQuestsHiddenTable {
    const val NAME = "osm_quests_hidden"

    object Columns {
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            $QUEST_TYPE varchar(255) NOT NULL,
            $ELEMENT_ID int NOT NULL,
            $ELEMENT_TYPE varchar(255) NOT NULL,
            CONSTRAINT same_osm_quest PRIMARY KEY ($QUEST_TYPE, $ELEMENT_ID, $ELEMENT_TYPE)
        );"""
}
