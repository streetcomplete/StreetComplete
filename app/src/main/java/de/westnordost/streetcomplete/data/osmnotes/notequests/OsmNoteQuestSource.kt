package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.osmapi.map.data.BoundingBox

interface OsmNoteQuestSource {
    interface Listener {
        fun onUpdated(addedQuests: Collection<OsmNoteQuest>, deletedQuestIds: Collection<Long>)
    }

    /** get single quest by id */
    fun get(questId: Long): OsmNoteQuest?

    /** Get count of all unanswered quests in given bounding box */
    fun getAllInBBoxCount(bbox: BoundingBox): Int

    /** Get all unanswered quests in given bounding box */
    fun getAllInBBox(bbox: BoundingBox): List<OsmNoteQuest>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
