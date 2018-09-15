package de.westnordost.streetcomplete.data.osmnotes;

import android.content.SharedPreferences;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.osmapi.notes.NotesDao;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.VisibleQuestListener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OsmNotesDownloadTest extends TestCase
{
	private NoteDao noteDB;
	private OsmNoteQuestDao noteQuestDB;
	private CreateNoteDao createNoteDB;
	private SharedPreferences preferences;
	private OsmAvatarsDownload avatarsDownload;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		noteDB = mock(NoteDao.class);
		noteQuestDB = mock(OsmNoteQuestDao.class);
		createNoteDB = mock(CreateNoteDao.class);
		preferences = mock(SharedPreferences.class);
		avatarsDownload = mock(OsmAvatarsDownload.class);
	}

	public void testDeleteObsoleteQuests()
	{
		when(preferences.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)).thenReturn(true);

		// in the quest database mock, there are quests for note 4 and note 5
		List<OsmNoteQuest> quests = new ArrayList<>();
		Note note1 = createANote();
		note1.id = 4L;
		quests.add(new OsmNoteQuest(12L, note1, QuestStatus.NEW, null, new Date(), new OsmNoteQuestType(), null));
		Note note2 = createANote();
		note2.id = 5L;
		quests.add(new OsmNoteQuest(13L, note2, QuestStatus.NEW, null, new Date(), new OsmNoteQuestType(), null));
		when(noteQuestDB.getAll(any(BoundingBox.class), any(QuestStatus.class)))
				.thenReturn(quests);

		doAnswer(invocation ->
		{
			Collection<Long> deletedQuests = (Collection<Long>) (invocation.getArguments()[0]);
			assertEquals(1, deletedQuests.size());
			assertEquals(13L, (long) deletedQuests.iterator().next());
			return 1;
		}).when(noteQuestDB).deleteAll(any(Collection.class));

		// note dao mock will only "find" the note #4
		List<Note> notes = new ArrayList<>();
		notes.add(note1);
		NotesDao noteServer = new TestListBasedNotesDao(notes);

		OsmNotesDownload dl = new OsmNotesDownload(
				noteServer, noteDB, noteQuestDB, createNoteDB, preferences, new OsmNoteQuestType(), avatarsDownload);

		VisibleQuestListener listener = mock(VisibleQuestListener.class);
		dl.setQuestListener(listener);

		dl.download(new BoundingBox(0,0,1,1), null, 1000);

		verify(noteQuestDB).deleteAll(any(Collection.class));
		verify(listener).onQuestsRemoved(any(Collection.class), any(QuestGroup.class));
	}

	private Note createANote()
	{
		Note note = new Note();
		note.id = 4;
		note.position = new OsmLatLon(6.0,7.0);
		note.status = Note.Status.OPEN;
		note.dateCreated = new Date();
		NoteComment comment = new NoteComment();
		comment.date = new Date();
		comment.action = NoteComment.Action.OPENED;
		comment.text = "hurp durp";
		note.comments.add(comment);
		return note;
	}

	private static class TestListBasedNotesDao extends NotesDao
	{
		List<Note> notes;

		public TestListBasedNotesDao(List<Note> notes)
		{
			super(null);
			this.notes = notes;
		}

		@Override public void getAll(BoundingBox bounds, Handler<Note> handler, int limit,
									 int hideClosedNoteAfter)
		{
			// ignoring all the parameters except the handler...
			for(Note note : notes)
			{
				handler.handle(note);
			}
		}
	}
}
