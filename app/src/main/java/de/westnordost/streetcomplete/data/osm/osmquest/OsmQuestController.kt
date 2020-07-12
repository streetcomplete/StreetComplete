package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.quest.QuestStatus
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for managing OsmQuests. Takes care of persisting OsmQuest objects along with their
 *  referenced geometry and notifying listeners about changes */
@Singleton class OsmQuestController @Inject internal constructor(
    private val dao: OsmQuestDao,
    private val geometryDao: ElementGeometryDao
){
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    interface QuestStatusListener {
        /** the status of a quest was changed */
        fun onChanged(quest: OsmQuest, previousStatus: QuestStatus)
        /** a quest was removed */
        fun onRemoved(questId: Long, previousStatus: QuestStatus)
        /** various note quests were updated */
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
        quest.status = QuestStatus.REVERT
        dao.update(quest)
        onChanged(quest, status)
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

    /** Replace all quests of the given type in the given bounding box with the given quests,
     *  including their geometry. Called on download of a quest type for a bounding box. */
    fun replaceInBBox(quests: List<OsmQuest>, bbox: BoundingBox, questType: String): UpdateResult {
        /* All quests in the given bounding box and of the given type should be replaced by the
        *  input list. So, there may be 1. new quests that are added and 2. there may be previous
        *  quests that have been there before but now not anymore, these need to be removed. */

        val previousQuestIdsByElement = dao.getAll(
            bounds = bbox,
            questTypes = listOf(questType)
        ).associate { ElementKey(it.elementType, it.elementId)  to it.id!! }.toMutableMap()
        val addedQuests = mutableListOf<OsmQuest>()

        for (quest in quests) {
            val e = ElementKey(quest.elementType, quest.elementId)
            if (previousQuestIdsByElement.containsKey(e)) {
                previousQuestIdsByElement.remove(e)
            } else {
                addedQuests.add(quest)
            }
        }
        val obsoleteQuestIds = previousQuestIdsByElement.values

        val deletedCount = removeObsolete(obsoleteQuestIds)
        val addedCount = addNew(addedQuests)

        /* Only send quests to listener that were really added, i.e. have an ID. How could quests
        *  not be added at this point? If they exist in the DB but outside the bounding box,
        *  so usually either if the geometry has been moved in the meantime, or, some quests
        *  also extend the bbox in which they download the quests, like the housenumber quest */
        val reallyAddedQuests = addedQuests.filter { it.id != null }

        onUpdated(added = reallyAddedQuests, deleted = obsoleteQuestIds)

        return UpdateResult(added = addedCount, deleted = deletedCount)
    }

    /** Add new unanswered quests and remove others for the given element, including their linked
     *  geometry. Called when an OSM element is updated, so the quests that reference that element
     *  need to be updated as well. */
    fun updateForElement(added: List<OsmQuest>, removedIds: List<Long>, elementType: Element.Type, elementId: Long): UpdateResult {
        val e = ElementKey(elementType, elementId)

        var deletedCount = removeObsolete(removedIds)
        /* Before new quests are unlocked, all reverted quests need to be removed for this element
           so that they can be created anew as the case may be */
        deletedCount += dao.deleteAll(statusIn = listOf(QuestStatus.REVERT), element = e)
        val addedCount = addNew(added)
        onUpdated(added = added, deleted = removedIds)

        return UpdateResult(added = addedCount, deleted = deletedCount)
    }

    /** Add new unanswered quests, including their linked geometry */
    private fun addNew(quests: Collection<OsmQuest>): Int {
        if (quests.isNotEmpty()) {
            geometryDao.putAll(quests.map { ElementGeometryEntry(it.elementType, it.elementId, it.geometry) })
            return dao.addAll(quests)
        }
        return 0
    }

    /** Remove obsolete quests, including their linked geometry */
    private fun removeObsolete(questIds: Collection<Long>): Int {
        if (questIds.isNotEmpty()) {
            val result = dao.deleteAllIds(questIds)
            if (result > 0) {
                geometryDao.deleteUnreferenced()
            }
            return result
        }
        return 0
    }

    /** Remove all unsolved quests that reference the given element. Used for when a quest blocker
     * (=a note) or similar has been added for that element position. */
    fun deleteAllUnsolvedForElement(elementType: Element.Type, elementId: Long): Int {
        val deletedQuestIds = dao.getAllIds(
            statusIn = listOf(QuestStatus.NEW),
            element = ElementKey(elementType, elementId)
        )
        val result = dao.deleteAllIds(deletedQuestIds)
        geometryDao.deleteUnreferenced()
        onUpdated(deleted = deletedQuestIds)
        return result
    }

    /** Remove all quests that reference the given element. Used for when that element has been
     *  deleted, so all quests referencing this element should to. */
    fun deleteAllForElement(elementType: Element.Type, elementId: Long): Int {
        val deletedQuestIds = dao.getAllIds(element = ElementKey(elementType, elementId))
        val result = dao.deleteAllIds(deletedQuestIds)
        geometryDao.delete(elementType, elementId)
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
            statusIn = listOf(QuestStatus.CLOSED, QuestStatus.REVERT),
            changedBefore = oldUploadedQuestsTimestamp
        )
        // remove old unsolved and hidden quests. To let the quest cache not get too big and to
        // ensure that the probability of merge conflicts remains low
        val oldUnsolvedQuestsTimestamp = System.currentTimeMillis() - ApplicationConstants.DELETE_UNSOLVED_QUESTS_AFTER
        deleted += dao.deleteAll(
            statusIn = listOf(QuestStatus.NEW, QuestStatus.HIDDEN),
            changedBefore = oldUnsolvedQuestsTimestamp
        )
        if (deleted > 0) geometryDao.deleteUnreferenced()

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


    /** Get count of all unanswered quests in given bounding box of given types */
    fun getAllVisibleInBBoxCount(bbox: BoundingBox, questTypes: Collection<String>) : Int =
        dao.getCount(
            statusIn = listOf(QuestStatus.NEW),
            bounds = bbox,
            questTypes = questTypes
        )

    /** Get all unanswered quests in given bounding box of given types */
    fun getAllVisibleInBBox(bbox: BoundingBox, questTypes: Collection<String>): List<OsmQuest> =
        dao.getAll(
            statusIn = listOf(QuestStatus.NEW),
            bounds = bbox,
            questTypes = questTypes
        )

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

    /** Get all quests for the given type */
    fun getAllForElement(elementType: Element.Type, elementId: Long): List<OsmQuest> =
        dao.getAll(element = ElementKey(elementType, elementId))
            .filter { it.status != QuestStatus.REVERT }


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

    data class UpdateResult(val added: Int, val deleted: Int)
}

