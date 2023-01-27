package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import java.util.concurrent.CopyOnWriteArrayList

/** Used to get visible osm note quests */
class OsmNoteQuestController(
    private val noteSource: NotesWithEditsSource,
    private val hiddenDB: NoteQuestsHiddenDao,
    private val userDataSource: UserDataSource,
    private val userLoginStatusSource: UserLoginStatusSource,
    private val notesPreferences: NotesPreferences,
) : OsmNoteQuestSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    interface HideOsmNoteQuestListener {
        fun onHid(edit: OsmNoteQuestHidden)
        fun onUnhid(edit: OsmNoteQuestHidden)
        fun onUnhidAll()
    }
    private val hideListeners: MutableList<HideOsmNoteQuestListener> = CopyOnWriteArrayList()

    private val listeners: MutableList<OsmNoteQuestSource.Listener> = CopyOnWriteArrayList()

    private val showOnlyNotesPhrasedAsQuestions: Boolean get() =
        notesPreferences.showOnlyNotesPhrasedAsQuestions

    private val noteUpdatesListener = object : NotesWithEditsSource.Listener {
        override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
            val hiddenNoteIds = getHiddenIds()

            val quests = mutableListOf<OsmNoteQuest>()
            val deletedQuestIds = ArrayList(deleted)
            for (note in added) {
                val q = createQuestForNote(note, hiddenNoteIds)
                if (q != null) quests.add(q)
            }
            for (note in updated) {
                val q = createQuestForNote(note, hiddenNoteIds)
                if (q != null) quests.add(q)
                else deletedQuestIds.add(note.id)
            }
            onUpdated(quests, deletedQuestIds)
        }

        override fun onCleared() {
            listeners.forEach { it.onInvalidated() }
        }
    }

    private val userLoginStatusListener = object : UserLoginStatusSource.Listener {
        override fun onLoggedIn() {
            // notes created by the user in this app or commented on by this user should not be shown
            onInvalidated()
        }
        override fun onLoggedOut() {}
    }

    private val notesPreferencesListener = object : NotesPreferences.Listener {
        override fun onNotesPreferencesChanged() {
            // a lot of notes become visible/invisible if this option is changed
            onInvalidated()
        }
    }

    init {
        noteSource.addListener(noteUpdatesListener)
        userLoginStatusSource.addListener(userLoginStatusListener)
        notesPreferences.listener = notesPreferencesListener
    }

    override fun getVisible(questId: Long): OsmNoteQuest? {
        if (isHidden(questId)) return null
        return noteSource.get(questId)?.let { createQuestForNote(it) }
    }

    override fun getAllVisibleInBBox(bbox: BoundingBox): List<OsmNoteQuest> {
        return createQuestsForNotes(noteSource.getAll(bbox))
    }

    private fun createQuestsForNotes(notes: Collection<Note>): List<OsmNoteQuest> {
        val blockedNoteIds = getHiddenIds()
        return notes.mapNotNull { createQuestForNote(it, blockedNoteIds) }
    }

    private fun createQuestForNote(note: Note, blockedNoteIds: Set<Long> = setOf()): OsmNoteQuest? =
        if (note.shouldShowAsQuest(userDataSource.userId, showOnlyNotesPhrasedAsQuestions, blockedNoteIds)) {
            OsmNoteQuest(note.id, note.position)
        } else null

    /* ----------------------------------- Hiding / Unhiding  ----------------------------------- */

    /** Mark the quest as hidden by user interaction */
    fun hide(questId: Long) {
        val hidden: OsmNoteQuestHidden?
        synchronized(this) {
            hiddenDB.add(questId)
            hidden = getHidden(questId)
        }
        if (hidden != null) onHid(hidden)
        onUpdated(deletedQuestIds = listOf(questId))
    }

    /** Un-hides a specific hidden quest by user interaction */
    fun unhide(questId: Long): Boolean {
        val hidden = getHidden(questId)
        synchronized(this) {
            if (!hiddenDB.delete(questId)) return false
        }
        if (hidden != null) onUnhid(hidden)
        val quest = noteSource.get(questId)?.let { createQuestForNote(it, emptySet()) }
        if (quest != null) onUpdated(quests = listOf(quest))
        return true
    }

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int {
        val previouslyHiddenNotes = noteSource.getAll(hiddenDB.getAllIds())
        val unhidCount = synchronized(this) { hiddenDB.deleteAll() }

        val unhiddenNoteQuests = previouslyHiddenNotes.mapNotNull { createQuestForNote(it, emptySet()) }

        onUnhidAll()
        onUpdated(quests = unhiddenNoteQuests)
        return unhidCount
    }

    fun getHidden(questId: Long): OsmNoteQuestHidden? {
        val timestamp = hiddenDB.getTimestamp(questId) ?: return null
        val note = noteSource.get(questId) ?: return null
        return OsmNoteQuestHidden(note, timestamp)
    }

    fun getAllHiddenNewerThan(timestamp: Long): List<OsmNoteQuestHidden> {
        val noteIdsWithTimestamp = hiddenDB.getNewerThan(timestamp)
        val notesById = noteSource.getAll(noteIdsWithTimestamp.map { it.noteId }).associateBy { it.id }

        return noteIdsWithTimestamp.mapNotNull { (noteId, timestamp) ->
            notesById[noteId]?.let { OsmNoteQuestHidden(it, timestamp) }
        }
    }

    private fun isHidden(questId: Long): Boolean = hiddenDB.contains(questId)

    private fun getHiddenIds(): Set<Long> = hiddenDB.getAllIds().toSet()

    /* ---------------------------------------- Listener ---------------------------------------- */

    override fun addListener(listener: OsmNoteQuestSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: OsmNoteQuestSource.Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(
        quests: Collection<OsmNoteQuest> = emptyList(),
        deletedQuestIds: Collection<Long> = emptyList()
    ) {
        if (quests.isEmpty() && deletedQuestIds.isEmpty()) return
        listeners.forEach { it.onUpdated(quests, deletedQuestIds) }
    }

    private fun onInvalidated() {
        listeners.forEach { it.onInvalidated() }
    }

    /* ------------------------------------- Hide Listeners ------------------------------------- */

    fun addHideQuestsListener(listener: HideOsmNoteQuestListener) {
        hideListeners.add(listener)
    }
    fun removeHideQuestsListener(listener: HideOsmNoteQuestListener) {
        hideListeners.remove(listener)
    }

    private fun onHid(edit: OsmNoteQuestHidden) {
        hideListeners.forEach { it.onHid(edit) }
    }
    private fun onUnhid(edit: OsmNoteQuestHidden) {
        hideListeners.forEach { it.onUnhid(edit) }
    }
    private fun onUnhidAll() {
        hideListeners.forEach { it.onUnhidAll() }
    }
}

private fun Note.shouldShowAsQuest(
    userId: Long,
    showOnlyNotesPhrasedAsQuestions: Boolean,
    blockedNoteIds: Set<Long>
): Boolean {
    // don't show notes hidden by user
    if (id in blockedNoteIds) return false

    /* don't show notes where user replied last unless he wrote a survey required marker */
    if (comments.last().isReplyFromUser(userId)
        && !comments.last().containsSurveyRequiredMarker()
    ) return false

    /* newly created notes by user should not be shown if it was both created in this app and has no
       replies yet */
    if (probablyCreatedByUserInThisApp(userId) && !hasReplies) return false

    /* many notes are created to report problems on the map that cannot be resolved
     * through an on-site survey.
     * Likely, if something is posed as a question, the reporter expects someone to
     * answer/comment on it, possibly an information on-site is missing, so let's only show these */
    if (showOnlyNotesPhrasedAsQuestions
        && !probablyContainsQuestion()
        && !containsSurveyRequiredMarker()
    ) return false

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

    val text = comments.firstOrNull()?.text
    return text?.matches(Regex(".*$questionMarksAroundTheWorld.*", RegexOption.DOT_MATCHES_ALL)) ?: false
}

private fun Note.containsSurveyRequiredMarker(): Boolean =
    comments.any { it.containsSurveyRequiredMarker() }

private fun NoteComment.containsSurveyRequiredMarker(): Boolean =
    text?.matches(".*#surveyme.*".toRegex()) == true

private fun Note.probablyCreatedByUserInThisApp(userId: Long): Boolean {
    val firstComment = comments.first()
    val isViaApp = firstComment.text?.contains("via " + ApplicationConstants.NAME) == true
    return firstComment.isFromUser(userId) && isViaApp
}

private val Note.hasReplies: Boolean get() =
    comments.any { it.isReply }

private fun NoteComment.isReplyFromUser(userId: Long): Boolean =
    isFromUser(userId) && isReply

private val NoteComment.isReply: Boolean get() =
    action == NoteComment.Action.COMMENTED

private fun NoteComment.isFromUser(userId: Long): Boolean =
    user?.id == userId
