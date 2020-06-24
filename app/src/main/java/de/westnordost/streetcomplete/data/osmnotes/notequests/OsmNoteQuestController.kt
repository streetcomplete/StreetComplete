package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osmnotes.NoteDao
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.quests.note_discussion.NoteAnswer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for managing OsmNoteQuests. Takes care of persisting OsmNoteQuest objects along with
 *  their referenced OSM notes and notifying listeners about changes. */
@Singleton class OsmNoteQuestController @Inject internal constructor(
    private val dao: OsmNoteQuestDao,
    private val noteDao: NoteDao
) {
    /* There is a 1:1 relationship of OsmNoteQuests and Notes, so how they are persisted in the
    *  background is an implementation detail. So, this class manages the quests together with the
    *  notes. */

    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

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
    private val questStatusListeners: MutableList<QuestStatusListener> = CopyOnWriteArrayList()

    /* ---------------------------------- Modify single quests ---------------------------------- */

    /** Mark the quest as answered by the user with the given answer */
    fun answer(quest: OsmNoteQuest, answer: NoteAnswer) {
        val status = quest.status
        quest.comment = answer.text
        quest.imagePaths = answer.imagePaths
        quest.status = QuestStatus.ANSWERED
        dao.update(quest)
        onChanged(quest, status)
    }

    /** Mark the quest as hidden by user interaction */
    fun hide(quest: OsmNoteQuest) {
        val status = quest.status
        quest.status = QuestStatus.HIDDEN
        dao.update(quest)
        onChanged(quest, status)
    }

    /** Mark that the upload of the quest was successful */
    fun success(quest: OsmNoteQuest) {
        val status = quest.status
        quest.status = QuestStatus.CLOSED
        noteDao.put(quest.note)
        dao.update(quest)
        onChanged(quest, status)
    }

    /** Mark that the quest failed because there was an unsolvable conflict during upload (deletes
     *  the quest) */
    fun fail(quest: OsmNoteQuest) {
        val status = quest.status
        dao.delete(quest.id!!)
        noteDao.delete(quest.note.id)
        onRemoved(quest.id!!, status)
    }

    /* ------------------------------------------------------------------------------------------ */

    /** Add a single quest, including its note */
    fun add(quest: OsmNoteQuest) {
        noteDao.put(quest.note)
        dao.add(quest)
        onAdded(quest)
    }

    /** Replace all quests in the given bounding box with the given quests, including its notes */
    fun replaceInBBox(quests: List<OsmNoteQuest>, bbox: BoundingBox): UpdateResult {
        /* All quests in the given bounding box and of the given type should be replaced by the
        *  input list. So, there may be 1. new quests that are added because there are new notes,
        *   2. there may be previous quests that are no more because the notes have been
        *   closed/deleted and 3. existing quests that have become invisible/solved because the
        *   notes have been solved outside the app */

        val previousQuestsByNoteId = dao.getAll(bounds = bbox)
            .associateBy { it.note.id }
            .toMutableMap()

        val closedExistingQuests = mutableListOf<OsmNoteQuest>()
        val addedQuests = mutableListOf<OsmNoteQuest>()

        for (quest in quests) {
            val existingQuest = previousQuestsByNoteId[quest.note.id]
            if (existingQuest != null) {
                previousQuestsByNoteId.remove(quest.note.id)
                /* The status of note quests can have changed from outside this app since they are tied
                *  to notes. So if the note changed, i.e. if a user answered or closed the note from the
                *  website instead of within the app. This needs to be accounted for here.
                * */
                if (quest.status == QuestStatus.CLOSED || quest.status == QuestStatus.INVISIBLE) {
                    existingQuest.status = quest.status
                    closedExistingQuests.add(existingQuest)
                }
            } else {
                addedQuests.add(quest)
            }
        }
        val obsoleteQuestIds = previousQuestsByNoteId.values.map { it.id!! }

        // delete obsolete note quests (note is not there anymore)
        var deletedCount = 0
        if (obsoleteQuestIds.isNotEmpty()) {
            deletedCount = dao.deleteAllIds(obsoleteQuestIds)
            if (deletedCount > 0) {
                noteDao.deleteUnreferenced()
            }
        }
        // update all notes (they may have new comments etc)
        if (quests.isNotEmpty()) {
            noteDao.putAll(quests.map { it.note })
        }
        var closedCount = 0
        if (closedExistingQuests.isNotEmpty()) {
            closedCount = dao.updateAll(closedExistingQuests)
        }
        var addedCount = 0
        if (addedQuests.isNotEmpty()) {
            addedCount = dao.addAll(addedQuests)
        }

        onUpdated(added = addedQuests, updated = closedExistingQuests, deleted = obsoleteQuestIds)

        return UpdateResult(added = addedCount, deleted = deletedCount, closed = closedCount)
    }

    /** Make all the given quests invisible */
    fun makeAllInvisible(quests: List<OsmNoteQuest>) {
        quests.forEach { it.status = QuestStatus.INVISIBLE }
        dao.updateAll(quests)
        onUpdated(updated = quests)
    }

    /** Make all invisible quests visible again */
    fun makeAllInvisibleVisible() {
        val quests = dao.getAll(statusIn = listOf(QuestStatus.INVISIBLE))
        quests.forEach { it.status = QuestStatus.NEW }
        dao.updateAll(quests)
        onUpdated(updated = quests)
    }

    /** Delete old unsolved and hidden quests */
    fun cleanUp(): Int {
        // remove old unsolved and hidden quests
        val oldUnsolvedQuestsTimestamp = System.currentTimeMillis() - ApplicationConstants.DELETE_UNSOLVED_QUESTS_AFTER
        val deletedQuestIds = dao.getAllIds(
            statusIn = listOf(QuestStatus.NEW, QuestStatus.HIDDEN),
            changedBefore = oldUnsolvedQuestsTimestamp
        )
        val deleted = dao.deleteAllIds(deletedQuestIds)
        onUpdated(deleted = deletedQuestIds)
        return deleted
    }

    /* ---------------------------------------- Getters ----------------------------------------- */

    /** Get all unanswered quests in given bounding box */
    fun getAllVisibleInBBoxCount(bbox: BoundingBox): Int =
        dao.getCount(statusIn = listOf(QuestStatus.NEW), bounds = bbox)

    /** Get all unanswered quests in given bounding box */
    fun getAllVisibleInBBox(bbox: BoundingBox): List<OsmNoteQuest> =
        dao.getAll(statusIn = listOf(QuestStatus.NEW), bounds = bbox)

    /** Get single quest by id */
    fun get(id: Long): OsmNoteQuest? = dao.get(id)

    /** Get all answered quests */
    fun getAllAnswered(): List<OsmNoteQuest> = dao.getAll(statusIn = listOf(QuestStatus.ANSWERED))

    /** Get all answered quests count */
    fun getAllAnsweredCount(): Int = dao.getCount(statusIn = listOf(QuestStatus.ANSWERED))

    /** Get all unanswered quests */
    fun getAllVisible(): List<OsmNoteQuest> =  dao.getAll(statusIn = listOf(QuestStatus.NEW))

    /* ------------------------------------ Listeners ------------------------------------------- */

    fun addQuestStatusListener(listener: QuestStatusListener) {
        questStatusListeners.add(listener)
    }
    fun removeQuestStatusListener(listener: QuestStatusListener) {
        questStatusListeners.remove(listener)
    }

    private fun onAdded(quest: OsmNoteQuest) {
        questStatusListeners.forEach { it.onAdded(quest) }
    }
    private fun onChanged(quest: OsmNoteQuest, previousStatus: QuestStatus) {
        questStatusListeners.forEach { it.onChanged(quest, previousStatus) }
    }
    private fun onRemoved(id: Long, previousStatus: QuestStatus) {
        questStatusListeners.forEach { it.onRemoved(id, previousStatus) }
    }
    private fun onUpdated(added: Collection<OsmNoteQuest> = listOf(), updated: Collection<OsmNoteQuest> = listOf(), deleted: Collection<Long> = listOf()) {
        questStatusListeners.forEach { it.onUpdated(added,updated, deleted) }
    }

    data class UpdateResult(val added: Int, val deleted: Int, val closed: Int)
}