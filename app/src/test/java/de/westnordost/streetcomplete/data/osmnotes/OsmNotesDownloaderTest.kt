package de.westnordost.streetcomplete.data.osmnotes

import android.content.SharedPreferences
import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.osmapi.notes.NotesDao
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.*
import org.mockito.Mockito.verify
import org.mockito.invocation.InvocationOnMock
import java.util.*

class OsmNotesDownloaderTest {
    private lateinit var noteDB: NoteDao
    private lateinit var notesServer: NotesDao
    private lateinit var noteQuestDB: OsmNoteQuestDao
    private lateinit var createNoteDB: CreateNoteDao
    private lateinit var preferences: SharedPreferences
    private lateinit var avatarsDownloader: OsmAvatarsDownloader

    @Before fun setUp() {
        noteDB = mock()
        notesServer = mock()
        noteQuestDB = mock()
        createNoteDB = mock()
        preferences = mock()
        avatarsDownloader = mock()
    }

    @Test fun `delete obsolete quests`() {
        on(preferences.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)).thenReturn(true)
        // in the quest database mock, there are quests for note 4 and note 5

        val note1 = createANote(4L)
        val note2 = createANote(5L)

        on(noteQuestDB.getAll(isNull(), any(), isNull())).thenReturn(listOf(
            OsmNoteQuest(12L, note1, QuestStatus.NEW, null, Date(), OsmNoteQuestType(), null),
            OsmNoteQuest(13L, note2, QuestStatus.NEW, null, Date(), OsmNoteQuestType(), null)
        ))

        doAnswer { invocation: InvocationOnMock ->
            val deletedQuests = invocation.arguments[0] as Collection<Long>
            assertEquals(1, deletedQuests.size)
            assertEquals(13L, deletedQuests.iterator().next())
            1
        }.on(noteQuestDB).deleteAllIds(any())

        // note dao mock will only "find" the note #4
        val noteServer: NotesDao = TestListBasedNotesDao(listOf(note1))

        val dl = OsmNotesDownloader(noteServer, noteDB, noteQuestDB, createNoteDB, preferences, OsmNoteQuestType(), avatarsDownloader)
        dl.questListener = mock()
        dl.download(BoundingBox(0.0, 0.0, 1.0, 1.0), null, 1000)

        verify(noteQuestDB).deleteAllIds(any())
        verify(dl.questListener!!).onQuestsRemoved(any(), any())
    }
}

private fun createANote(id: Long): Note {
    val note = Note()
    note.id = id
    note.position = OsmLatLon(6.0, 7.0)
    note.status = Note.Status.OPEN
    note.dateCreated = Date()
    val comment = NoteComment()
    comment.date = Date()
    comment.action = NoteComment.Action.OPENED
    comment.text = "hurp durp"
    note.comments.add(comment)
    return note
}

private class TestListBasedNotesDao(val notes: List<Note>) :  NotesDao(null) {
    override fun getAll(bounds: BoundingBox, handler: Handler<Note>, limit: Int, hideClosedNoteAfter: Int) {
        for (note in notes) {
            handler.handle(note)
        }
    }
}
