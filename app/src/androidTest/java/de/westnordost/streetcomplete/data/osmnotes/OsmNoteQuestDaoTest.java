package de.westnordost.streetcomplete.data.osmnotes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.westnordost.streetcomplete.data.ApplicationDbTestCase;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.notes.Note;

public class OsmNoteQuestDaoTest extends ApplicationDbTestCase
{
	private OsmNoteQuestDao dao;
	private NoteDao noteDao;
	private OsmNoteQuestType questType;

	@Override public void setUp() throws Exception
	{
		super.setUp();
		questType = new OsmNoteQuestType();
		dao = new OsmNoteQuestDao(dbHelper, serializer, questType);
		noteDao = new NoteDao(dbHelper, serializer);
	}

	public void testAddGetNoChanges()
	{
		Note note = NoteDaoTest.createNote();
		OsmNoteQuest quest = new OsmNoteQuest(note, questType);
		noteDao.put(note);
		dao.add(quest);
		OsmNoteQuest dbQuest = dao.get(quest.getId());

		checkEqual(quest, dbQuest);
	}

	public void testAddGetWithChanges()
	{
		Note note = NoteDaoTest.createNote();
		ArrayList<String> imagePaths = new ArrayList<>();
		imagePaths.add("blubbi");
		imagePaths.add("diblub");
		OsmNoteQuest quest = new OsmNoteQuest(null, note, QuestStatus.ANSWERED, "hi da du", new Date(1234), questType, imagePaths);
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

		OsmNoteQuest quest = new OsmNoteQuest(note,questType);
		dao.add(quest);

		OsmNoteQuest questForSameNote = new OsmNoteQuest(note, questType);
		questForSameNote.setStatus(QuestStatus.HIDDEN);
		boolean result = dao.add(questForSameNote);

		List<OsmNoteQuest> quests = dao.getAll(null, null);
		assertEquals(1, quests.size());
		assertEquals(QuestStatus.NEW, quests.get(0).getStatus());
		assertFalse(result);
		assertNull(questForSameNote.getId());
	}

	public void testAddReplace()
	{
		Note note = NoteDaoTest.createNote();
		noteDao.put(note);

		OsmNoteQuest quest = new OsmNoteQuest(note, questType);
		dao.add(quest);

		OsmNoteQuest questForSameNote = new OsmNoteQuest(note, questType);
		questForSameNote.setStatus(QuestStatus.HIDDEN);
		boolean result = dao.replace(questForSameNote);

		List<OsmNoteQuest> quests = dao.getAll(null, null);
		assertEquals(1, quests.size());
		assertEquals(QuestStatus.HIDDEN, quests.get(0).getStatus());
		assertTrue(result);
	}

	public void testGetPositions()
	{
		Note note = NoteDaoTest.createNote();
		note.position = new OsmLatLon(34,35);
		noteDao.put(note);
		OsmNoteQuest quest = new OsmNoteQuest(note, questType);
		dao.add(quest);
		List<LatLon> positions = dao.getAllPositions(new BoundingBox(0,0,50,50));
		assertEquals(1,positions.size());
		assertEquals(new OsmLatLon(34,35), positions.get(0));
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
