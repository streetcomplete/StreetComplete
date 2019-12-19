package de.westnordost.streetcomplete.data.osmnotes

import android.content.SharedPreferences
import android.util.Log

import javax.inject.Inject

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.osmapi.notes.NotesDao

class OsmNotesDownloader @Inject constructor(
    private val noteServer: NotesDao,
    private val noteDB: NoteDao,
    private val noteQuestDB: OsmNoteQuestDao,
    private val createNoteDB: CreateNoteDao,
    private val preferences: SharedPreferences,
    private val questType: OsmNoteQuestType,
    private val avatarsDownloader: OsmAvatarsDownloader
) {

    var questListener: VisibleQuestListener? = null

    fun download(bbox: BoundingBox, userId: Long?, max: Int): Set<LatLon> {
        val positions = HashSet<LatLon>()
        val previousQuestsByNoteId = getPreviousQuestsByNoteId(bbox).toMutableMap()
        val notes = ArrayList<Note>()
        val quests = ArrayList<OsmNoteQuest>()
        val hiddenQuests = ArrayList<OsmNoteQuest>()
        val noteCommentUserIds = HashSet<Long>()

        noteServer.getAll(bbox, { note ->
            if (note.comments.isNotEmpty()) { // exclude invalid notes (#1338)
                val quest = OsmNoteQuest(note, questType)
                if (shouldMakeNoteClosed(userId, note)) {
                    quest.status = QuestStatus.CLOSED
                    hiddenQuests.add(quest)
                } else if (shouldMakeNoteInvisible(quest)) {
                    quest.status = QuestStatus.INVISIBLE
                    hiddenQuests.add(quest)
                } else {
                    quests.add(quest)
                    previousQuestsByNoteId.remove(note.id)
                }
                for (comment in note.comments) {
                    if (comment.user != null) noteCommentUserIds.add(comment.user.id)
                }
                notes.add(note)
                positions.add(note.position)
            }
        }, max, 0)

        noteDB.putAll(notes)
        val hiddenAmount = noteQuestDB.replaceAll(hiddenQuests)
        val newAmount = noteQuestDB.addAll(quests)
        val visibleAmount = quests.size

        if (questListener != null) {
            val questsCreated = quests.filter { it.id != null }

            if (questsCreated.isNotEmpty()) questListener?.onQuestsCreated(questsCreated, QuestGroup.OSM_NOTE)
            /* we do not call listener.onNoteQuestRemoved for hiddenQuests here, because on
            *  replacing hiddenQuests into DB, they get new quest IDs. As far as the DB is concerned,
            *  hidden note quests are always new quests which are hidden.
            *  If a note quest was visible before, it'll be removed below when the previous quests
            *  are cleared */
        }

        /* delete note quests created in a previous run in the given bounding box that are not
           found again -> these notes have been closed/solved/removed */
        if (previousQuestsByNoteId.isNotEmpty()) {
            questListener?.onQuestsRemoved(previousQuestsByNoteId.values, QuestGroup.OSM_NOTE)

            noteQuestDB.deleteAllIds(previousQuestsByNoteId.values)
            noteDB.deleteUnreferenced()
        }

        for (createNote in createNoteDB.getAll(bbox)) {
            positions.add(createNote.position)
        }

        val closedAmount = previousQuestsByNoteId.size

        Log.i(TAG,
            "Successfully added $newAmount new and removed $closedAmount closed notes" +
            " ($hiddenAmount of ${hiddenAmount + visibleAmount} notes are hidden)"
        )

        avatarsDownloader.download(noteCommentUserIds)

        return positions
    }

    private fun getPreviousQuestsByNoteId(bbox: BoundingBox): Map<Long, Long> =
        noteQuestDB.getAll(bounds = bbox).associate { it.note.id to it.id!! }

    // the difference to hidden is that is that invisible quests may turn visible again, dependent
    // on the user's settings while hidden quests are "dead"
    private fun shouldMakeNoteInvisible(quest: OsmNoteQuest): Boolean {
        /* many notes are created to report problems on the map that cannot be resolved
         * through an on-site survey rather than questions from other (armchair) mappers
         * that want something cleared up on-site.
         * Likely, if something is posed as a question, the reporter expects someone to
         * answer/comment on it, so let's only show these */
        val showNonQuestionNotes = preferences.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)
        return !(quest.probablyContainsQuestion() || showNonQuestionNotes)
    }

    private fun shouldMakeNoteClosed(userId: Long?, note: Note): Boolean {
        if (userId == null) return false
        /* hide a note if he already contributed to it. This can also happen from outside
           this application, which is why we need to overwrite its quest status. */
        return note.containsCommentFromUser(userId) || note.probablyCreatedByUserInApp(userId)
    }

    companion object {
        private const val TAG = "QuestDownload"
    }
}

private fun Note.containsCommentFromUser(userId: Long): Boolean {
    for (comment in comments) {
        val isComment = comment.action == NoteComment.Action.COMMENTED
        if (comment.isFromUser(userId) && isComment) return true
    }
    return false
}

private fun Note.probablyCreatedByUserInApp(userId: Long): Boolean {
    val firstComment = comments.first()
    val isViaApp = firstComment.text.contains("via " + ApplicationConstants.NAME)
    return firstComment.isFromUser(userId) && isViaApp
}

private fun NoteComment.isFromUser(userId: Long): Boolean {
    return user != null && user.id == userId
}
