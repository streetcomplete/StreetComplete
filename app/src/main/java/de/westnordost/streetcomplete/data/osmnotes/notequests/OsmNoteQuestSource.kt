package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.quest.QuestStatus

interface OsmNoteQuestSource {

    /** Interface for listening to changes for quest (status). */
    interface QuestStatusListener {
        /** a note quest was added */
        fun onAdded(quest: OsmNoteQuest)
        /** the status of a note quest was changed */
        fun onChanged(quest: OsmNoteQuest, previousStatus: QuestStatus)
        /** a note quest was removed */
        fun onRemoved(questId: Long, previousStatus: QuestStatus)
        /** various note quests were updated */
        fun onUpdated(added: Collection<OsmNoteQuest>, updated: Collection<OsmNoteQuest>, deleted: Collection<Long>)
    }

    fun addQuestStatusListener(listener: QuestStatusListener)
    fun removeQuestStatusListener(listener: QuestStatusListener)

    /** Get single quest by id */
    fun get(questId: Long): OsmNoteQuest?

    /** Get all answered quests */
    fun getAllAnswered(): List<OsmNoteQuest>

    /** Get all answered quests count */
    fun getAllAnsweredCount(): Int

    /** Get all unanswered quests in given bounding box */
    fun getAllVisibleInBBox(bbox: BoundingBox): List<OsmNoteQuest>

    /** Get count of all unanswered quests in given bounding box */
    fun getAllVisibleInBBoxCount(bbox: BoundingBox): Int
}
