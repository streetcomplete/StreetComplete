package de.westnordost.streetcomplete.data.osmnotes;

import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NotesDao;
import de.westnordost.streetcomplete.data.QuestStatus;

import static org.mockito.Mockito.*;

public class OsmNoteQuestChangesUploadTest extends TestCase
{
	public void testCancel() throws InterruptedException
	{
		OsmNoteQuestDao questDb = mock(OsmNoteQuestDao.class);
		when(questDb.getAll(null, QuestStatus.ANSWERED)).thenAnswer(
				new Answer<List<OsmNoteQuest>>()
				{
					@Override public List<OsmNoteQuest> answer(InvocationOnMock invocation) throws Throwable
					{
						Thread.sleep(1000); // take your time...
						ArrayList<OsmNoteQuest> result = new ArrayList<>();
						result.add(null);
						return result;
					}
				});

		final OsmNoteQuestChangesUpload u = new OsmNoteQuestChangesUpload(null, questDb, null);
		final AtomicBoolean cancel = new AtomicBoolean(false);

		Thread t = new Thread(new Runnable()
		{
			@Override public void run()
			{
				u.upload(cancel);
			}
		});
		t.start();

		cancel.set(true);
		// cancelling the thread works if we come out here without exceptions. If the note upload
		// would actually try to start anything, there would be a nullpointer exception since we
		// feeded it only with nulls to work with
		t.join();
	}

	public void testDropCommentWhenConflict()
	{
		OsmNoteQuest quest = createQuest();

		NotesDao osmDao = mock(NotesDao.class);
		when(osmDao.comment(anyLong(), anyString())).thenThrow(OsmConflictException.class);
		OsmNoteQuestDao questDb = mock(OsmNoteQuestDao.class);
		NoteDao noteDb = mock(NoteDao.class);

		assertNull(new OsmNoteQuestChangesUpload(osmDao, questDb, noteDb).uploadNoteChanges(quest));

		verify(questDb).delete(quest.getId());
		verify(noteDb).delete(quest.getNote().id);
	}

	public void testUploadComment()
	{
		OsmNoteQuest quest = createQuest();

		NotesDao osmDao = mock(NotesDao.class);
		when(osmDao.comment(anyLong(), anyString())).thenReturn(mock(Note.class));
		OsmNoteQuestDao questDb = mock(OsmNoteQuestDao.class);
		NoteDao noteDb = mock(NoteDao.class);

		Note n = new OsmNoteQuestChangesUpload(osmDao, questDb, noteDb).uploadNoteChanges(quest);
		assertNotNull(n);
		assertEquals(n, quest.getNote());
		assertEquals(QuestStatus.CLOSED, quest.getStatus());
		verify(questDb).update(quest);
		verify(noteDb).put(n);
	}

	private OsmNoteQuest createQuest()
	{
		Note note = new Note();
		note.id = 1;
		note.position = new OsmLatLon(1.0, 2.0);
		OsmNoteQuest quest = new OsmNoteQuest(note);
		quest.setId(3);
		quest.setStatus(QuestStatus.NEW);
		quest.setComment("blablub");
		return quest;
	}
}
