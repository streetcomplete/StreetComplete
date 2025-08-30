package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

interface AtpQuestSource {
    // TODO API REQUIRES API see OsmQuestSource and OsmNoteQuestSource
    interface Listener {
        fun onUpdated(added: Collection<CreateElementUsingAtpQuest>, deleted: Collection<Long>)
        fun onInvalidated()
    }

    /** get single quest by id if not hidden by user */
    fun get(questId: Long): CreateElementUsingAtpQuest?

    /** Get all quests in given bounding box */
    fun getAllInBBox(bbox: BoundingBox): List<CreateElementUsingAtpQuest>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
