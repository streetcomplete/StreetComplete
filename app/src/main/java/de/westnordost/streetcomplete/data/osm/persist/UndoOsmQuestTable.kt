package de.westnordost.streetcomplete.data.osm.persist


object UndoOsmQuestTable {
    const val NAME = "osm_quests_undo"
    const val NAME_MERGED_VIEW = "osm_quests_full_undo"

    object Columns {
        const val QUEST_ID = "quest_id"
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val TAG_CHANGES = "tag_changes"
        const val CHANGES_SOURCE = "changes_source"
    }
}
