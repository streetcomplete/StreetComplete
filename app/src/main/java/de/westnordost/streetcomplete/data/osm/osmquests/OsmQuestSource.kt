package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

interface OsmQuestSource {

    interface Listener {
        fun onUpdated(addedQuests: Collection<OsmQuest>, deletedQuestKeys: Collection<OsmQuestKey>)
        fun onInvalidated()
    }

    /** get single quest by id */
    fun get(key: OsmQuestKey): OsmQuest?

    /** Get count of all quests in given bounding box */
    fun getAllInBBoxCount(bbox: BoundingBox): Int

    /** Get all quests of optionally the given types in given bounding box */
    fun getAllVisibleInBBox(bbox: BoundingBox, questTypes: Collection<String>? = null): List<OsmQuest>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
