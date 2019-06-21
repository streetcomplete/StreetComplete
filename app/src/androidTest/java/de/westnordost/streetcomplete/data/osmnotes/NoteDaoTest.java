package de.westnordost.streetcomplete.data.osmnotes;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.ListIterator;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;
import de.westnordost.osmapi.notes.NoteComment;
import de.westnordost.osmapi.user.User;

import static org.junit.Assert.*;

public class NoteDaoTest extends ApplicationDbTestCase
{
	private NoteDao dao;

	@Before public void createDao()
	{
		dao = new NoteDao(dbHelper, serializer);
	}

	@Test public void putGetNoClosedDate()
	{
		Note note = createNote();

		dao.put(note);
		Note dbNote = dao.get(note.id);
		checkEqual(note, dbNote);
	}

	@Test public void putAll()
	{
		Collection<Note> notes = new ArrayList<>();
		Note n1 = createNote();
		n1.id = 1;
		notes.add(n1);
		Note n2 = createNote();
		n2.id = 2;
		notes.add(n2);

		dao.putAll(notes);
		assertNotNull(dao.get(1));
		assertNotNull(dao.get(2));
	}

	@Test public void putReplace()
	{
		Note note = createNote();
		dao.put(note);
		note.status = Note.Status.CLOSED;
		dao.put(note);

		Note dbNote = dao.get(note.id);
		checkEqual(note, dbNote);
	}

	@Test public void putGetWithClosedDate()
	{
		Note note = createNote();
		note.dateClosed = new Date(6000);

		dao.put(note);
		Note dbNote = dao.get(note.id);
		checkEqual(note, dbNote);
	}

	@Test public void deleteUnreferenced()
	{
		Note note = createNote();
		dao.put(note);
		assertEquals(1,dao.deleteUnreferenced());

		dao.put(note);
		new OsmNoteQuestDao(dbHelper,serializer, new OsmNoteQuestType())
				.add(new OsmNoteQuest(note, new OsmNoteQuestType()));
		assertEquals(0,dao.deleteUnreferenced());
	}

	@Test public void delete()
	{
		Note note = createNote();
		assertFalse(dao.delete(note.id));
		dao.put(note);
		assertTrue(dao.delete(note.id));
		assertNull(dao.get(note.id));
	}

	private void checkEqual(Note note, Note dbNote)
	{
		assertEquals(note.id, dbNote.id);
		assertEquals(note.position, dbNote.position);
		assertEquals(note.status, dbNote.status);
		assertEquals(note.dateCreated, dbNote.dateCreated);
		assertEquals(note.dateClosed, dbNote.dateClosed);

		assertEquals(note.comments.size(), dbNote.comments.size());
		ListIterator<NoteComment> it, dbIt;
		it = note.comments.listIterator();
		dbIt = dbNote.comments.listIterator();

		while(it.hasNext() && dbIt.hasNext())
		{
			NoteComment comment = it.next();
			NoteComment dbComment = dbIt.next();
			assertEquals(comment.action, dbComment.action);
			assertEquals(comment.date, dbComment.date);
			assertEquals(comment.text, dbComment.text);
			assertEquals(comment.user.displayName, dbComment.user.displayName);
			assertEquals(comment.user.id, dbComment.user.id);
		}
	}

	static Note createNote()
	{
		Note note = new Note();
		note.position = new OsmLatLon(1,1);
		note.status = Note.Status.OPEN;
		note.id = 5;
		note.dateCreated = new Date(5000);

		NoteComment comment = new NoteComment();
		comment.text = "hi";
		comment.date = new Date(5000);
		comment.action = NoteComment.Action.OPENED;
		comment.user = new User(5,"PingPong");
		note.comments.add(comment);

		return note;
	}
}
