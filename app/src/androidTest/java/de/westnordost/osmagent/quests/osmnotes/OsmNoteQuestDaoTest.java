package de.westnordost.osmagent.quests.osmnotes;

import java.util.Date;

import de.westnordost.osmagent.quests.OsmagentDbTestCase;
import de.westnordost.osmagent.quests.QuestStatus;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;

public class OsmNoteQuestDaoTest extends OsmagentDbTestCase
{
	private OsmNoteQuestDao dao;
	private NoteDao noteDao;

	@Override public void setUp()
	{
		super.setUp();
		dao = new OsmNoteQuestDao(dbHelper, serializer);
		noteDao = new NoteDao(dbHelper, serializer);
	}

	public void testAddGetNoChanges()
	{
		Note note = NoteDaoTest.createNote();
		OsmNoteQuest quest = new OsmNoteQuest(note);
		noteDao.put(note);
		quest.setId(dao.add(quest));
		OsmNoteQuest dbQuest = dao.get(quest.getId());

		checkEqual(quest, dbQuest);
	}

	public void testAddGetWithChanges()
	{
		Note note = NoteDaoTest.createNote();
		NoteChange change = new NoteChange(NoteChange.Action.COMMENT, "hi da du");

		OsmNoteQuest quest = new OsmNoteQuest(null, note, QuestStatus.ANSWERED, change, new Date(1234));
		noteDao.put(note);
		quest.setId(dao.add(quest));
		OsmNoteQuest dbQuest = dao.get(quest.getId());

		checkEqual(quest, dbQuest);
	}

	public void testNoNote()
	{
		OsmNoteQuest quest = new OsmNoteQuest(new OsmLatLon(3,3), "open this note here...");
		quest.setId(dao.add(quest));
		OsmNoteQuest dbQuest = dao.get(quest.getId());
		checkEqual(quest, dbQuest);
	}

	private void checkEqual(OsmNoteQuest quest, OsmNoteQuest dbQuest)
	{
		assertEquals(quest.getLastUpdate(), dbQuest.getLastUpdate());
		assertEquals(quest.getStatus(), dbQuest.getStatus());
		assertEquals(quest.getMarkerLocation(), dbQuest.getMarkerLocation());
		assertEquals(quest.getChanges(), dbQuest.getChanges());
		assertEquals(quest.getId(), dbQuest.getId());
		assertEquals(quest.getType(), dbQuest.getType());
		// note saving already tested in NoteDaoTest
	}
}
