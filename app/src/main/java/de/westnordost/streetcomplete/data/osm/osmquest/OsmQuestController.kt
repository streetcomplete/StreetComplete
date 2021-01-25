package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.quest.QuestStatus
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for managing OsmQuests. Takes care of persisting OsmQuest objects and notifying
 *  listeners about changes */
@Singleton class OsmQuestController @Inject internal constructor(private val dao: OsmQuestDao) {

    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    interface QuestStatusListener {
        /** the status of a quest was changed */
        fun onChanged(quest: OsmQuest, previousStatus: QuestStatus)
        /** a quest was removed */
        fun onRemoved(questId: Long, previousStatus: QuestStatus)
        /** various quests were updated */
        fun onUpdated(added: Collection<OsmQuest>, updated: Collection<OsmQuest>, deleted: Collection<Long>)
    }
    private val questStatusListeners: MutableList<QuestStatusListener> = CopyOnWriteArrayList()

    /* ---------------------------------- Modify single quests ---------------------------------- */

    /** Return the previously answered quest to the initial unanswered state */
    fun undo(quest: OsmQuest) {
        val status = quest.status
        quest.status = QuestStatus.NEW
        quest.changes = null
        quest.changesSource = null
        dao.update(quest)
        onChanged(quest, status)
    }

    /** Mark the previously successfully uploaded quest as reverted */
    fun revert(quest: OsmQuest) {
        val status = quest.status
        dao.delete(quest.id!!)
        onRemoved(quest.id!!, status)
    }

    /** Mark the quest as answered by the user with the given answer */
    fun answer(quest: OsmQuest, changes: StringMapChanges, source: String) {
        val status = quest.status
        quest.changes = changes
        quest.changesSource = source
        quest.status = QuestStatus.ANSWERED
        dao.update(quest)
        onChanged(quest, status)
    }

    /** Mark the quest as hidden by user interaction */
    fun hide(quest: OsmQuest) {
        val status = quest.status
        quest.status = QuestStatus.HIDDEN
        dao.update(quest)
        onChanged(quest, status)
    }

    /** Mark that the upload of the quest was successful */
    fun success(quest: OsmQuest) {
        val status = quest.status
        quest.status = QuestStatus.CLOSED
        dao.update(quest)
        onChanged(quest, status)
    }

    /** Mark that the quest failed because either the changes could not be applied or there was an
     *  unsolvable conflict during upload (deletes the quest) */
    fun fail(quest: OsmQuest) {
        val status = quest.status
        /* #812 conflicting quests may not reside in the database, otherwise they would
           wrongfully be candidates for an undo - even though nothing was changed */
        dao.delete(quest.id!!)
        onRemoved(quest.id!!, status)
    }


    /* ------------------------------------------------------------------------------------------ */

    /** Replace all quests of the given types in the given bounding box with the given quests.
     *  Called on download of a quest type for a bounding box. */
    fun updateForBBox(bbox: BoundingBox, quests: Collection<OsmQuest>) {
        val time = System.currentTimeMillis()

        val previousQuests = mutableMapOf<OsmElementQuestType<*>, MutableMap<ElementKey, OsmQuest>>()
        for (quest in dao.getAll(bounds = bbox)) {
            val previousQuestIdsByElement = previousQuests.getOrPut(quest.osmElementQuestType, { mutableMapOf() })
            previousQuestIdsByElement[ElementKey(quest.elementType, quest.elementId)] = quest
        }

        for (quest in quests) {
            previousQuests[quest.osmElementQuestType]?.remove(ElementKey(quest.elementType, quest.elementId))
        }
        val obsoleteQuestIds = previousQuests.values
            .flatMap { it.values }
            .filter { it.status.isUnanswered } // do not delete quests with changes
            .map { it.id!! }

        val deletedCount = dao.deleteAllIds(obsoleteQuestIds)
        val addedCount = dao.addAll(quests)
        val reallyAddedQuests = quests.filter { it.id != null }

        val seconds = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Added $addedCount new and removed $deletedCount already resolved quests in ${seconds}s")

        onUpdated(added = reallyAddedQuests, deleted = obsoleteQuestIds)
    }

    /** For the given element, replace the current quests with the given ones. Called when an OSM
     *  element is updated, so the quests that reference that element need to be updated as well. */
    fun updateForElement(elementType: Element.Type, elementId: Long, quests: List<OsmQuest>) {
        val elementKey = ElementKey(elementType, elementId)
        val previousQuestsByType = dao.getAll(element = elementKey)
            .associateBy { it.osmElementQuestType }
            .toMutableMap()

        for (quest in quests) {
            previousQuestsByType.remove(quest.osmElementQuestType)
        }
        val obsoleteQuests = previousQuestsByType.values.filter { it.status.isUnanswered } // do not delete quests with changes
        val obsoleteQuestIds = obsoleteQuests.mapNotNull { it.id }

        dao.deleteAllIds(obsoleteQuestIds)
        dao.addAll(quests) // if already exists, will not add it
        val reallyAddedQuests = quests.filter { it.id != null }

        Log.i(TAG,
            "For ${elementType.name}#$elementId:"
                + if (reallyAddedQuests.isEmpty()) "" else " added: ${reallyAddedQuests.joinToString { it.javaClass.name }}"
                + if (obsoleteQuests.isEmpty()) "" else " removed: ${obsoleteQuests.joinToString { it.javaClass.name }}"
        )

        onUpdated(added = reallyAddedQuests, deleted = obsoleteQuestIds)
    }

    /** Remove all unsolved quests that reference the given element. Used for when a quest blocker
     * (=a note) or similar has been added for that element position. */
    fun deleteAllUnsolvedForElement(elementType: Element.Type, elementId: Long): Int {
        val deletedQuestIds = dao.getAllIds(
            statusIn = listOf(QuestStatus.NEW, QuestStatus.HIDDEN, QuestStatus.INVISIBLE),
            element = ElementKey(elementType, elementId)
        )
        val result = dao.deleteAllIds(deletedQuestIds)
        onUpdated(deleted = deletedQuestIds)
        return result
    }

    /** Remove all quests that reference the given element. Used for when that element has been
     *  deleted, so all quests referencing this element should to. */
    fun deleteAllForElement(elementType: Element.Type, elementId: Long): Int {
        val deletedQuestIds = dao.getAllIds(element = ElementKey(elementType, elementId))
        val result = dao.deleteAllIds(deletedQuestIds)
        onUpdated(deleted = deletedQuestIds)
        return result
    }

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int {
        val hiddenQuests = dao.getAll(statusIn = listOf(QuestStatus.HIDDEN))
        hiddenQuests.forEach { it.status = QuestStatus.NEW }
        val result = dao.updateAll(hiddenQuests)
        onUpdated(updated = hiddenQuests)
        return result
    }

    /** Delete old closed and reverted quests and old unsolved and hidden quests */
    fun cleanUp(): Int {
        // remove old uploaded quests. These were kept only for the undo/history function
        val oldUploadedQuestsTimestamp = System.currentTimeMillis() - ApplicationConstants.MAX_QUEST_UNDO_HISTORY_AGE
        var deleted = dao.deleteAll(
            statusIn = listOf(QuestStatus.CLOSED),
            changedBefore = oldUploadedQuestsTimestamp
        )
        // remove old unsolved and hidden quests. To let the quest cache not get too big and to
        // ensure that the probability of merge conflicts remains low
        val oldUnsolvedQuestsTimestamp = System.currentTimeMillis() - ApplicationConstants.DELETE_UNSOLVED_QUESTS_AFTER
        deleted += dao.deleteAll(
            statusIn = listOf(QuestStatus.NEW, QuestStatus.HIDDEN),
            changedBefore = oldUnsolvedQuestsTimestamp
        )

        onUpdated()

        return deleted
    }

    /* ---------------------------------------- Getters ----------------------------------------- */

    /** Get the quest types of all unsolved quests for the given element */
    fun getAllUnsolvedQuestTypesForElement(elementType: Element.Type, elementId: Long): List<OsmElementQuestType<*>> {
        return dao.getAll(
            statusIn = listOf(QuestStatus.NEW),
            element = ElementKey(elementType, elementId)
        ).map { it.osmElementQuestType }
    }


    /** Get count of all unanswered quests in given bounding box  */
    fun getAllVisibleInBBoxCount(bbox: BoundingBox) : Int {
        return dao.getCount(
            statusIn = listOf(QuestStatus.NEW),
            bounds = bbox
        )
    }

    /** Get all unanswered quests in given bounding box of given types */
    fun getAllVisibleInBBox(bbox: BoundingBox, questTypes: Collection<String>): List<OsmQuest> {
        if (questTypes.isEmpty()) return listOf()
        return dao.getAll(
            statusIn = listOf(QuestStatus.NEW),
            bounds = bbox,
            questTypes = questTypes
        )
    }


    /** Get single quest by id */
    fun get(id: Long): OsmQuest? = dao.get(id)

    /** Get the last undoable quest (includes answered, hidden and uploaded) */
    fun getLastUndoable(): OsmQuest? = dao.getLastSolved()

    /** Get all undoable quests count */
    fun getAllUndoableCount(): Int = dao.getCount(statusIn = listOf(
        QuestStatus.ANSWERED, QuestStatus.CLOSED, QuestStatus.HIDDEN
    ))

    /** Get all answered quests */
    fun getAllAnswered(): List<OsmQuest> = dao.getAll(statusIn = listOf(QuestStatus.ANSWERED))

    /** Get all answered quests count */
    fun getAllAnsweredCount(): Int = dao.getCount(statusIn = listOf(QuestStatus.ANSWERED))

    /* ------------------------------------ Listeners ------------------------------------------- */

    fun addQuestStatusListener(listener: QuestStatusListener) {
        questStatusListeners.add(listener)
    }
    fun removeQuestStatusListener(listener: QuestStatusListener) {
        questStatusListeners.remove(listener)
    }

    private fun onChanged(quest: OsmQuest, previousStatus: QuestStatus) {
        questStatusListeners.forEach { it.onChanged(quest, previousStatus) }
    }
    private fun onRemoved(id: Long, previousStatus: QuestStatus) {
        questStatusListeners.forEach { it.onRemoved(id, previousStatus) }
    }
    private fun onUpdated(
        added: Collection<OsmQuest> = listOf(),
        updated: Collection<OsmQuest> = listOf(),
        deleted: Collection<Long> = listOf()
    ) {
        questStatusListeners.forEach { it.onUpdated(added, updated, deleted) }
    }

    companion object {
        private const val TAG = "OsmQuestController"
    }
}

