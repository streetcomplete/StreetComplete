package de.westnordost.streetcomplete.data.osmnotes.notequests

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.util.Listeners

/** Used to get visible osm note quests */
class OsmNoteQuestController(
    private val noteSource: NotesWithEditsSource,
    private val userDataSource: UserDataSource,
    private val userLoginSource: UserLoginSource,
    private val prefs: Preferences,
) : OsmNoteQuestSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val listeners = Listeners<OsmNoteQuestSource.Listener>()

    private val showOnlyNotesPhrasedAsQuestions: Boolean get() =
        !prefs.showAllNotes

    private val settingsListener: SettingsListener

    private val noteUpdatesListener = object : NotesWithEditsSource.Listener {
        override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
            val quests = mutableListOf<OsmNoteQuest>()
            val deletedQuestIds = ArrayList(deleted)
            for (note in added) {
                val q = createQuestForNote(note)
                if (q != null) quests.add(q)
            }
            for (note in updated) {
                val q = createQuestForNote(note)
                if (q != null) {
                    quests.add(q)
                } else {
                    deletedQuestIds.add(note.id)
                }
            }
            onUpdated(quests, deletedQuestIds)
        }

        override fun onCleared() {
            listeners.forEach { it.onInvalidated() }
        }
    }

    private val userLoginStatusListener = object : UserLoginSource.Listener {
        override fun onLoggedIn() {
            // notes created by the user in this app or commented on by this user should not be shown
            onInvalidated()
        }
        override fun onLoggedOut() {}
    }

    init {
        noteSource.addListener(noteUpdatesListener)
        userLoginSource.addListener(userLoginStatusListener)
        // a lot of notes become visible/invisible if this option is changed
        settingsListener = prefs.onAllShowNotesChanged { onInvalidated() }
    }

    override fun get(questId: Long): OsmNoteQuest? =
        noteSource.get(questId)?.let { createQuestForNote(it) }

    override fun getAllInBBox(bbox: BoundingBox): List<OsmNoteQuest> =
        createQuestsForNotes(noteSource.getAll(bbox))

    private fun createQuestsForNotes(notes: Collection<Note>): List<OsmNoteQuest> =
        notes.mapNotNull { createQuestForNote(it) }

    private fun createQuestForNote(note: Note): OsmNoteQuest? =
        if (note.shouldShowAsQuest(userDataSource.userId, showOnlyNotesPhrasedAsQuestions)) {
            OsmNoteQuest(note.id, note.position)
        } else {
            null
        }

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
}

private fun Note.shouldShowAsQuest(
    userId: Long,
    showOnlyNotesPhrasedAsQuestions: Boolean
): Boolean {
    /*
        We usually don't show notes where either the user is the last responder, or the
        note was created with the app and has no replies.
        If the most recent comment contains "#surveyme", we want to show the note regardless.
        See https://github.com/streetcomplete/StreetComplete/issues/6052#issuecomment-2567163451
     */
    if (
        (
            comments.last().isReplyFromUser(userId) ||
            (probablyCreatedByUserInThisApp(userId) && !hasReplies)
        )
        && !comments.last().containsSurveyRequiredMarker()
    ) {
        return false
    }

    /*
        many notes are created to report problems on the map that cannot be resolved
        through an on-site survey.
        Likely, if something is posed as a question, the reporter expects someone to
        answer/comment on it, possibly an information on-site is missing, so let's only show these
     */
    if (showOnlyNotesPhrasedAsQuestions
        && !probablyContainsQuestion()
        && !containsSurveyRequiredMarker()
    ) {
        return false
    }

    return true
}

private fun Note.probablyContainsQuestion(): Boolean {
    /**
     * Source: https://en.wikipedia.org/wiki/Question_mark
     *
     * NOTE: some languages, like Thai, do not use any question mark, so this would be more
     * difficult to determine.
     */
    val questionMarksAroundTheWorld = listOf(
        "?", // Latin question mark
        "¿", // Spanish question mark in case the closing latin one was omitted
        ";", // Greek question mark (a different character than semicolon, though same appearance)
        ";", // semicolon (often used instead of proper greek question mark)
        "؟", // mirrored question mark (used in script written from right to left, like Arabic)
        "՞", // Armenian question mark
        "፧", // Ethiopian question mark
        "꘏", // Vai question mark
        "？", // full width question mark (often used in modern Chinese / Japanese)
    )
    val questionMarkPattern = ".*[${questionMarksAroundTheWorld.joinToString("")}].*"

    val text = comments.firstOrNull()?.text
    return text?.matches(questionMarkPattern.toRegex(RegexOption.DOT_MATCHES_ALL)) ?: false
}

private fun Note.containsSurveyRequiredMarker(): Boolean =
    comments.any { it.containsSurveyRequiredMarker() }

private fun NoteComment.containsSurveyRequiredMarker(): Boolean =
    text?.contains("#surveyme", ignoreCase = true) == true

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
