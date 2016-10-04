package de.westnordost.osmagent.quests.osmnotes;

import java.util.Date;
import java.util.List;

import de.westnordost.osmagent.quests.OsmagentDbTestCase;
import de.westnordost.osmagent.quests.QuestStatus;
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
		dao.add(quest);
		OsmNoteQuest dbQuest = dao.get(quest.getId());

		checkEqual(quest, dbQuest);
	}

	public void testAddGetWithChanges()
	{
		Note note = NoteDaoTest.createNote();
		OsmNoteQuest quest = new OsmNoteQuest(null, note, QuestStatus.ANSWERED, "hi da du", new Date(1234));
		noteDao.put(note);
		dao.add(quest);
		OsmNoteQuest dbQuest = dao.get(quest.getId());

		checkEqual(quest, dbQuest);
	}

	public void testAddTwice()
	{
		// tests if the "unique" property is set correctly in the table

		Note note = NoteDaoTest.createNote();
		noteDao.put(note);

		OsmNoteQuest quest = new OsmNoteQuest(note);
		dao.add(quest);

		OsmNoteQuest questForSameNote = new OsmNoteQuest(note);
		questForSameNote.setStatus(QuestStatus.HIDDEN);
		boolean result = dao.add(questForSameNote);

		List<OsmNoteQuest> quests = dao.getAll(null, null);
		assertEquals(1, quests.size());
		assertEquals(QuestStatus.NEW, quests.get(0).getStatus());
		assertFalse(result);
	}

	private void checkEqual(OsmNoteQuest quest, OsmNoteQuest dbQuest)
	{
		assertEquals(quest.getLastUpdate(), dbQuest.getLastUpdate());
		assertEquals(quest.getStatus(), dbQuest.getStatus());
		assertEquals(quest.getMarkerLocation(), dbQuest.getMarkerLocation());
		assertEquals(quest.getComment(), dbQuest.getComment());
		assertEquals(quest.getId(), dbQuest.getId());
		assertEquals(quest.getType(), dbQuest.getType());
		// note saving already tested in NoteDaoTest
	}
}
