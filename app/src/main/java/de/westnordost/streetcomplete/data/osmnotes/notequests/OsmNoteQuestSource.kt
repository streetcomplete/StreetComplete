package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

interface OsmNoteQuestSource {
    interface Listener {
        fun onUpdated(addedQuests: Collection<OsmNoteQuest>, deletedQuestIds: Collection<Long>)
        fun onInvalidated()
    }

    /** get single quest by id */
    fun get(questId: Long): OsmNoteQuest?

    /** Get all quests in given bounding box */
    fun getAllVisibleInBBox(bbox: BoundingBox, getHidden: Boolean = false): List<OsmNoteQuest>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
