package de.westnordost.streetcomplete.data.osmnotes.notequests

import android.content.SharedPreferences
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osmnotes.NoteSource
import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.user.UserStore
import javax.inject.Inject

/** Updates note quests based on updated osm notes */
class OsmNoteQuestUpdater @Inject constructor(
    noteSource: NoteSource,
    private val questType: OsmNoteQuestType,
    private val osmNoteQuestController: OsmNoteQuestController,
    private val userStore: UserStore,
    private val preferences: SharedPreferences,
): NoteSource.NoteUpdatesListener {

    init {
        noteSource.addNoteUpdatesListener(this)
    }

    override fun onAdded(note: Note) {
        val userId = userStore.userId.takeIf { it != -1L }
        osmNoteQuestController.update(createQuestForNote(note, userId))
    }

    override fun onUpdated(note: Note) {
        val userId = userStore.userId.takeIf { it != -1L }
        osmNoteQuestController.update(createQuestForNote(note, userId))
    }

    override fun onDeleted(id: Long) {
        osmNoteQuestController.deleteForNote(id)
    }

    override fun onUpdatedForBBox(bbox: BoundingBox, notes: Collection<Note>) {
        val userId = userStore.userId.takeIf { it != -1L }
        val quests = mutableListOf<OsmNoteQuest>()
        for (note in notes) {
            quests.add(createQuestForNote(note, userId))
        }

        osmNoteQuestController.updateForBBox(bbox, quests)
    }

    private fun createQuestForNote(note: Note, userId: Long?): OsmNoteQuest {
        val quest = OsmNoteQuest(note, questType)
        if (shouldMakeNoteClosed(userId, note)) {
            quest.status = QuestStatus.CLOSED
        } else if (shouldMakeNoteInvisible(quest)) {
            quest.status = QuestStatus.INVISIBLE
        }
        return quest
    }

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
