package de.westnordost.streetcomplete.data.osmnotes.notequests

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.quests.note_discussion.NoteAnswer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for managing OsmNoteQuests. Takes care of persisting OsmNoteQuest and notifying
 *  listeners about changes. */
@Singleton class OsmNoteQuestController @Inject internal constructor(
    private val dao: OsmNoteQuestDao
) {
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

    /* ------------------------------------------------------------------------------------------ */

    fun deleteForNote(noteId: Long) {
        // TODO

        dao.delete(questId)
        onRemoved(questId, previousStatus)
    }

    fun update(newquest: OsmNoteQuest) {
        // TODO

        dao.update(quest)
        onChanged(quest, status)
    }

    /** Replace all quests in the given bounding box with the given quests */
    fun updateForBBox(bbox: BoundingBox, quests: List<OsmNoteQuest>) {
        val time = System.currentTimeMillis()

        val previousQuestsByNoteId = dao.getAll(bounds = bbox)
            .associateBy { it.note.id }
            .toMutableMap()

        /* this here would be a little simpler if SQLite on android supported UPSERT, now we have to
        *  sort by hand which quests are added and which quests are updated (and which did not change) */
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
        val obsoleteQuestIds = previousQuestsByNoteId.values
            .filter { it.status.isUnanswered } // do not delete quests with changes
            .map { it.id!! }

        val deletedCount = dao.deleteAllIds(obsoleteQuestIds)
        val closedCount = dao.updateAll(closedExistingQuests)
        val addedCount = dao.addAll(addedQuests)
        val reallyAddedQuests = quests.filter { it.id != null }

        val seconds = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Added $addedCount new, removed $deletedCount closed and hid $closedCount note quests in ${seconds}s")

        onUpdated(added = reallyAddedQuests, updated = closedExistingQuests, deleted = obsoleteQuestIds)
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

    companion object {
        private const val TAG = "OsmNoteQuestController"
    }
}
