package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestType

interface OsmQuestSource {

    interface Listener {
        fun onUpdated(added: Collection<OsmQuest>, deleted: Collection<OsmQuestKey>)
        fun onInvalidated()
    }

    /** get single quest by id */
    fun get(key: OsmQuestKey): OsmQuest?

    /** Get all quests of optionally the given types in given bounding box */
    fun getAllInBBox(bbox: BoundingBox, questTypes: Collection<QuestType>? = null): Collection<OsmQuest>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
