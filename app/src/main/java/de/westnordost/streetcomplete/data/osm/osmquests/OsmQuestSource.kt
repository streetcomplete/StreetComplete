package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

interface OsmQuestSource {

    interface Listener {
        fun onUpdated(addedQuests: Collection<OsmQuest>, deletedQuestKeys: Collection<OsmQuestKey>)
        fun onInvalidated()
    }

    /** get single quest by id if not hidden by user */
    fun getVisible(key: OsmQuestKey): OsmQuest?

    /** Get all quests of optionally the given types in given bounding box */
    fun getAllVisibleInBBox(bbox: BoundingBox, questTypes: Collection<String>? = null): List<OsmQuest>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
