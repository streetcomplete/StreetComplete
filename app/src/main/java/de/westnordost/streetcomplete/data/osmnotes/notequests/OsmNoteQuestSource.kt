package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

interface OsmNoteQuestSource {
    interface Listener {
        fun onUpdated(added: Collection<OsmNoteQuest>, deleted: Collection<Long>)
        fun onInvalidated()
    }

    /** get single quest by id if not hidden by user */
    fun get(questId: Long): OsmNoteQuest?

    /** Get all quests in given bounding box */
    fun getAllInBBox(bbox: BoundingBox): List<OsmNoteQuest>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
