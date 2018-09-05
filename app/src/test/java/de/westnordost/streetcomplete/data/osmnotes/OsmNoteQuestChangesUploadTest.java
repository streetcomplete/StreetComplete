package de.westnordost.streetcomplete.data.osmnotes;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NotesDao;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.util.ImageUploader;

import static org.mockito.Mockito.*;

public class OsmNoteQuestChangesUploadTest extends TestCase
{
	private ImageUploader imageUploader;
	private NoteDao noteDb;
	private OsmNoteQuestDao questDb;
	private QuestStatisticsDao questStatisticsDb;
	private NotesDao osmDao;

	private OsmNoteQuestChangesUpload osmNoteQuestChangesUpload;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		osmDao = mock(NotesDao.class);
		noteDb = mock(NoteDao.class);
		imageUploader = mock(ImageUploader.class);
		questDb = mock(OsmNoteQuestDao.class);
		questStatisticsDb = mock(QuestStatisticsDao.class);

		osmNoteQuestChangesUpload = new OsmNoteQuestChangesUpload(osmDao, questDb, questStatisticsDb, noteDb, imageUploader);
	}

	public void testCancel() throws InterruptedException
	{
		when(questDb.getAll(null, QuestStatus.ANSWERED)).thenAnswer( invocation ->
		{
			Thread.sleep(1000); // take your time...
			ArrayList<OsmNoteQuest> result = new ArrayList<>();
			result.add(null);
			return result;
		});

		final AtomicBoolean cancel = new AtomicBoolean(false);

		Thread t = new Thread(() -> osmNoteQuestChangesUpload.upload(cancel));
		t.start();

		cancel.set(true);
		// cancelling the thread works if we come out here without exceptions. If the note upload
		// would actually try to start anything, there would be a nullpointer exception since we
		// feeded it a null-quest
		t.join();
	}

	public void testDropCommentWhenConflict()
	{
		OsmNoteQuest quest = createQuest();

		when(osmDao.comment(anyLong(), anyString())).thenThrow(OsmConflictException.class);

		assertNull(osmNoteQuestChangesUpload.uploadNoteChanges(quest));

		verify(questDb).delete(quest.getId());
		verify(noteDb).delete(quest.getNote().id);
	}

	public void testUploadComment()
	{
		OsmNoteQuest quest = createQuest();

		when(osmDao.comment(anyLong(), anyString())).thenReturn(mock(Note.class));

		Note n = osmNoteQuestChangesUpload.uploadNoteChanges(quest);
		assertNotNull(n);
		assertEquals(n, quest.getNote());
		assertEquals(QuestStatus.CLOSED, quest.getStatus());
		verify(questDb).update(quest);
		verify(noteDb).put(n);
		verify(questStatisticsDb).addOneNote();
	}

	public void testUploadsImagesForComment()
	{
		OsmNoteQuest quest = createQuest();
		ArrayList<String> imagePaths = new ArrayList<>();
		imagePaths.add("Never say");
		quest.setImagePaths(imagePaths);


		when(imageUploader.upload(imagePaths)).thenReturn(Collections.singletonList("never"));

		osmNoteQuestChangesUpload.uploadNoteChanges(quest);

		verify(osmDao).comment(1, "blablub\n\nAttached photo(s):\nnever");
	}

	private static OsmNoteQuest createQuest()
	{
		Note note = new Note();
		note.id = 1;
		note.position = new OsmLatLon(1.0, 2.0);
		OsmNoteQuest quest = new OsmNoteQuest(note, new OsmNoteQuestType());
		quest.setId(3);
		quest.setStatus(QuestStatus.NEW);
		quest.setComment("blablub");
		return quest;
	}
}
