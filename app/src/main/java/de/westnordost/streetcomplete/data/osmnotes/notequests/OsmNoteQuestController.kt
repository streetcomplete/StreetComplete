package de.westnordost.streetcomplete.data.osmnotes.notequests

import android.content.SharedPreferences
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osmnotes.NoteSource
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteDao
import de.westnordost.streetcomplete.data.user.UserStore
import javax.inject.Inject
import javax.inject.Singleton

/** Used to get visible osm note quests */
@Singleton class OsmNoteQuestController @Inject constructor(
    private val noteSource: NoteSource,
    private val commentNoteDB: CommentNoteDao,
    private val hiddenNoteQuestDB: HiddenNoteQuestDao,
    private val questType: OsmNoteQuestType,
    private val userStore: UserStore,
    private val preferences: SharedPreferences,
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val showOnlyNotesPhrasedAsQuestions: Boolean get() =
        !preferences.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)

    private val userId: Long? get() = userStore.userId.takeIf { it != -1L }

    private val commentNoteListener = object : CommentNoteDao.Listener {
        override fun onAddedCommentNote() {
            TODO("Not yet implemented")
        }

        override fun onDeletedCommentNote() {
            TODO("Not yet implemented")
        }
    }

    private val noteUpdatesListener = object : NoteSource.NoteUpdatesListener {
        override fun onUpdatedForBBox(bbox: BoundingBox, notes: Collection<Note>) {
            val blockedNoteIds = getBlockedNoteIds()
            val quests = notes.mapNotNull { createQuestForNote(it, blockedNoteIds) }
            // TODO
        }

        override fun onUpdated(note: Note) {
            // TODO
        }

        override fun onDeleted(noteId: Long) {
            // TODO
        }
    }

    init {
        noteSource.addNoteUpdatesListener(noteUpdatesListener)
        commentNoteDB.addListener(commentNoteListener)
        preferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS) {
                TODO("Not yet implemented")
            }
        }
        userStore.addListener(object : UserStore.UpdateListener {
            override fun onUserDataUpdated() {
                TODO("Not yet implemented")
            }
        })
    }

    /** get single quest by id */
    fun get(questId: Long): OsmNoteQuest? {
        if (isNoteBlocked(questId)) return null
        return noteSource.get(questId)?.let { createQuestForNote(it, setOf()) }
    }

    /** Get count of all unanswered quests in given bounding box */
    fun getAllVisibleInBBoxCount(bbox: BoundingBox): Int {
        val blockedNoteIds = getBlockedNoteIds()
        return noteSource.getAll(bbox).mapNotNull { createQuestForNote(it, blockedNoteIds) }.size
    }

    /** Get all unanswered quests in given bounding box */
    fun getAllVisibleInBBox(bbox: BoundingBox): List<OsmNoteQuest> {
        return createQuestsForNotes(noteSource.getAll(bbox))
    }

    fun hide(noteId: Long) {
        hiddenNoteQuestDB.add(noteId)
        TODO("call listener")
    }

    fun unhideAll(): Int {
        return hiddenNoteQuestDB.deleteAll()
        TODO("call listener")
    }

    private fun createQuestsForNotes(notes: Collection<Note>): List<OsmNoteQuest> {
        val blockedNoteIds = getBlockedNoteIds()
        return notes.mapNotNull { createQuestForNote(it, blockedNoteIds) }
    }

    private fun createQuestForNote(note: Note, blockedNoteIds: Set<Long>): OsmNoteQuest? {
        val shouldShowQuest = note.shouldShowAsQuest(userId, showOnlyNotesPhrasedAsQuestions, blockedNoteIds)
        return if (shouldShowQuest) OsmNoteQuest(note, questType) else null
    }

    private fun isNoteBlocked(noteId: Long): Boolean =
        commentNoteDB.get(noteId) != null || hiddenNoteQuestDB.contains(noteId)

    private fun getBlockedNoteIds(): Set<Long> =
        (commentNoteDB.getAll().map { it.noteId } + hiddenNoteQuestDB.getAll()).toSet()
}

private fun Note.shouldShowAsQuest(
    userId: Long?,
    showOnlyNotesPhrasedAsQuestions: Boolean,
    blockedNoteIds: Set<Long>
): Boolean {

    // don't show a note if user already contributed to it
    if (userId != null) {
        if (containsCommentFromUser(userId) || probablyCreatedByUserInThisApp(userId)) return false
    }
    // a note comment pending to be uploaded also counts as contribution
    if (id in blockedNoteIds) return false

    /* many notes are created to report problems on the map that cannot be resolved
     * through an on-site survey.
     * Likely, if something is posed as a question, the reporter expects someone to
     * answer/comment on it, possibly an information on-site is missing, so let's only show these */
    if (showOnlyNotesPhrasedAsQuestions) {
        if (!probablyContainsQuestion()) return false
    }

    return true
}

private fun Note.probablyContainsQuestion(): Boolean {
    /* from left to right (if smartass IntelliJ wouldn't mess up left-to-right):
       - latin question mark
       - greek question mark (a different character than semikolon, though same appearance)
       - semikolon (often used instead of proper greek question mark)
       - mirrored question mark (used in script written from right to left, like Arabic)
       - armenian question mark
       - ethopian question mark
       - full width question mark (often used in modern Chinese / Japanese)
       (Source: https://en.wikipedia.org/wiki/Question_mark)

        NOTE: some languages, like Thai, do not use any question mark, so this would be more
        difficult to determine.
   */
    val questionMarksAroundTheWorld = "[?;;؟՞፧？]"

    val text = comments?.firstOrNull()?.text
    return text?.matches(".*$questionMarksAroundTheWorld.*".toRegex()) ?: false
}


private fun Note.containsCommentFromUser(userId: Long): Boolean {
    for (comment in comments) {
        val isComment = comment.action == NoteComment.Action.COMMENTED
        if (comment.isFromUser(userId) && isComment) return true
    }
    return false
}

private fun Note.probablyCreatedByUserInThisApp(userId: Long): Boolean {
    val firstComment = comments.first()
    val isViaApp = firstComment.text.contains("via " + ApplicationConstants.NAME)
    return firstComment.isFromUser(userId) && isViaApp
}

private fun NoteComment.isFromUser(userId: Long): Boolean {
    return user != null && user.id == userId
}
