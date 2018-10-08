package de.westnordost.streetcomplete.data.osmnotes;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.osmapi.notes.NotesDao;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.util.ImageUploader;

import static org.mockito.Mockito.*;

public class CreateNoteUploadTest extends TestCase
{
	private MapDataDao mapDataDao;
	private CreateNoteDao createNoteDb;
	private NotesDao notesDao;
	private OsmNoteQuestDao osmNoteQuestDb;
	private NoteDao noteDb;
	private QuestStatisticsDao questStatisticsDb;
	private ImageUploader imageUploader;

	private CreateNoteUpload createNoteUpload;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		mapDataDao = mock(MapDataDao.class);
		createNoteDb = mock(CreateNoteDao.class);
		notesDao = mock(NotesDao.class);
		osmNoteQuestDb = mock(OsmNoteQuestDao.class);
		noteDb = mock(NoteDao.class);
		imageUploader = mock(ImageUploader.class);
		questStatisticsDb = mock(QuestStatisticsDao.class);

		createNoteUpload = new CreateNoteUpload(createNoteDb, notesDao, noteDb, osmNoteQuestDb,
				mapDataDao, new OsmNoteQuestType(), questStatisticsDb, imageUploader);
	}


	public void testCancel() throws InterruptedException
	{
		when(createNoteDb.getAll(any(BoundingBox.class))).thenAnswer(invocation ->
		{
			Thread.sleep(1000); // take your time...
			ArrayList<CreateNote> result = new ArrayList<>();
			result.add(null);
			return result;
		});

		final AtomicBoolean cancel = new AtomicBoolean(false);

		Thread t = new Thread(() -> createNoteUpload.upload(cancel));
		t.start();

		cancel.set(true);
		// cancelling the thread works if we come out here without exceptions. If the note upload
		// would actually try to start anything, there would be a nullpointer exception since we
		// feeded it only with nulls to work with
		t.join();
	}

	public void testUploadNoteForDeletedElementWillCancel()
	{
		/* the mock for MapDataDao returns null for getNode, getWay, getRelation... by defaiöt */
		CreateNote createNote = createACreateNote();
		createNote.elementType = Element.Type.WAY;
		createNote.elementId = 5L;

		assertNull(createNoteUpload.uploadCreateNote(createNote));
		verifyNoteNotInsertedIntoDb(createNote.id);
	}

	public void testCreateNoteOnExistingNoteWillCommentOnExistingNote()
	{
		CreateNote createNote = createACreateNote();
		createNote.elementType = Element.Type.WAY;
		createNote.elementId = 5L;

		Note note = createNote(createNote);
		setUpThereIsANoteFor(createNote, note);

		when(notesDao.comment(anyLong(), anyString())).thenReturn(note);
		assertNotNull(createNoteUpload.uploadCreateNote(createNote));
		verify(notesDao).comment(note.id, createNote.text);

		verifyNoteInsertedIntoDb(createNote.id, note);
	}

	public void testCreateNoteOnExistingClosedNoteWillCancel()
	{
		CreateNote createNote = createACreateNote();
		createNote.elementType = Element.Type.WAY;
		createNote.elementId = 5L;

		Note note = createNote(createNote);
		note.status = Note.Status.CLOSED;
		setUpThereIsANoteFor(createNote, note);

		assertNull(createNoteUpload.uploadCreateNote(createNote));

		verify(notesDao).getAll(any(BoundingBox.class), any(Handler.class), anyInt(), anyInt());
		verifyNoMoreInteractions(notesDao);
		verifyNoteNotInsertedIntoDb(createNote.id);
	}

	public void testCreateNoteOnExistingNoteWillCancelWhenConflictException()
	{
		CreateNote createNote = createACreateNote();
		createNote.elementType = Element.Type.WAY;
		createNote.elementId = 5L;

		Note note = createNote(createNote);
		setUpThereIsANoteFor(createNote, note);

		when(notesDao.comment(anyLong(), anyString())).thenThrow(OsmConflictException.class);

		assertNull(createNoteUpload.uploadCreateNote(createNote));

		verify(notesDao).comment(note.id, createNote.text);

		verifyNoteNotInsertedIntoDb(createNote.id);
	}

	public void testCreateNoteWithNoAssociatedElement()
	{
		CreateNote createNote = createACreateNote();
		Note note = createNote(null);

		when(notesDao.create(any(LatLon.class), anyString())).thenReturn(note);

		assertNotNull(createNoteUpload.uploadCreateNote(createNote));

		verify(notesDao).create(createNote.position, createNote.text + "\n\nvia " + ApplicationConstants.USER_AGENT);

		verifyNoteInsertedIntoDb(createNote.id, note);
	}

	public void testCreateNoteWithNoQuestTitleButAssociatedElement()
	{
		CreateNote createNote = createACreateNote();
		createNote.elementType = Element.Type.WAY;
		createNote.elementId = 5L;
		when(mapDataDao.getWay(createNote.elementId)).thenReturn(mock(Way.class));

		Note note = createNote(null);

		when(notesDao.create(any(LatLon.class), anyString())).thenReturn(note);

		assertNotNull(createNoteUpload.uploadCreateNote(createNote));

		verify(notesDao).create(createNote.position,
				"for https://osm.org/way/5 via "+ ApplicationConstants.USER_AGENT+":\n\njo ho");

		verifyNoteInsertedIntoDb(createNote.id, note);
	}

	public void testCreateNoteWithAssociatedElementAndNoNoteYet()
	{
		CreateNote createNote = createACreateNote();
		createNote.elementType = Element.Type.WAY;
		createNote.elementId = 5L;
		createNote.questTitle = "What?";
		when(mapDataDao.getWay(createNote.elementId)).thenReturn(mock(Way.class));

		Note note = createNote(createNote);

		when(notesDao.create(any(LatLon.class), anyString())).thenReturn(note);

		assertNotNull(createNoteUpload.uploadCreateNote(createNote));

		verify(notesDao).create(createNote.position,
				"Unable to answer \"What?\" for https://osm.org/way/5 via "+ ApplicationConstants.USER_AGENT+":\n\njo ho");

		verifyNoteInsertedIntoDb(createNote.id, note);
	}

	public void testCreateNoteUploadsImagesAndDisplaysLinks()
	{
		CreateNote createNote = createACreateNote();
		createNote.imagePaths = new ArrayList<>();
		createNote.imagePaths.add("hello");

		Note note = createNote(null);
		when(notesDao.create(any(LatLon.class), anyString())).thenReturn(note);

		when(imageUploader.upload(createNote.imagePaths)).thenReturn(
				Collections.singletonList("hello, too")
		);

		assertNotNull(createNoteUpload.uploadCreateNote(createNote));

		verify(imageUploader).upload(createNote.imagePaths);

		verify(notesDao).create(createNote.position,"jo ho\n\nvia " + ApplicationConstants.USER_AGENT+"\n\nAttached photo(s):\nhello, too");
	}

	public void testCommentNoteUploadsImagesAndDisplaysLinks()
	{
		CreateNote createNote = createACreateNote();
		createNote.elementType = Element.Type.WAY;
		createNote.elementId = 5L;
		createNote.imagePaths = new ArrayList<>();
		createNote.imagePaths.add("hello");

		Note note = createNote(createNote);
		setUpThereIsANoteFor(createNote, note);

		when(imageUploader.upload(createNote.imagePaths)).thenReturn(
				Collections.singletonList("hello, too")
		);

		when(notesDao.comment(anyLong(), anyString())).thenReturn(note);
		assertNotNull(createNoteUpload.uploadCreateNote(createNote));

		verify(notesDao).comment(note.id ,"jo ho\n\nAttached photo(s):\nhello, too");
		verifyNoteInsertedIntoDb(createNote.id, note);
	}

	private void verifyNoteNotInsertedIntoDb(long createNoteId)
	{
		verifyNoMoreInteractions(noteDb, osmNoteQuestDb);
		verify(createNoteDb).delete(createNoteId);
	}

	private void verifyNoteInsertedIntoDb(long createNoteId, Note note)
	{
		verify(noteDb).put(note);
		verify(osmNoteQuestDb).add(any(OsmNoteQuest.class));
		verify(createNoteDb).delete(createNoteId);
		verify(questStatisticsDb).addOneNote();
	}

	private void setUpThereIsANoteFor(CreateNote createNote, final Note note)
	{
		when(mapDataDao.getWay(createNote.elementId)).thenReturn(mock(Way.class));
		doAnswer(invocation ->
		{
			Handler<Note> handler = (Handler<Note>) invocation.getArguments()[1];
			handler.handle(note);
			return null;
		}).when(notesDao).getAll(any(BoundingBox.class), any(Handler.class), anyInt(), anyInt());
	}

	private Note createNote(CreateNote fitsTo)
	{
		Note note = new Note();
		note.id = 2;
		note.status = Note.Status.OPEN;
		note.dateCreated = new Date();
		note.position = new OsmLatLon(1,2);
		NoteComment comment = new NoteComment();
		comment.text = "bla bla";
		if(fitsTo != null)
		{
			comment.text += CreateNoteUpload.getAssociatedElementString(fitsTo);
		}
		comment.action = NoteComment.Action.OPENED;
		comment.date = new Date();
		note.comments.add(0, comment);
		return note;
	}

	private CreateNote createACreateNote()
	{
		CreateNote n = new CreateNote();
		n.id = 1;
		n.text = "jo ho";
		n.position = new OsmLatLon(1.0,2.0);
		return n;
	}
}
