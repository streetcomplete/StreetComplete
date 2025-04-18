package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.OsmQuestKey

interface OsmQuestSource {

    interface Listener {
        fun onUpdated(added: Collection<OsmQuest>, deleted: Collection<OsmQuestKey>)
        fun onInvalidated()
    }

    /** get single quest by id */
    fun get(key: OsmQuestKey): OsmQuest?

    /** Get all quests of optionally the given types in given bounding box */
    fun getAllInBBox(bbox: BoundingBox, questTypes: Collection<String>? = null): List<OsmQuest>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
