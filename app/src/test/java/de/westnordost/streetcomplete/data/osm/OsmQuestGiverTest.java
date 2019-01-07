package de.westnordost.streetcomplete.data.osm;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OsmQuestGiverTest
{
	private static LatLon POS = new OsmLatLon(10,10);
	private static Node NODE = new OsmNode(1, 0, POS, null, null, null);

	private OsmNoteQuestDao osmNoteQuestDao;
	private OsmQuestDao osmQuestDao;
	private OsmQuestGiver osmQuestUnlocker;
	private OsmElementQuestType questType;

	@Before public void setUp() throws Exception
	{
		ElementGeometryDao elementGeometryDao = mock(ElementGeometryDao.class);
		when(elementGeometryDao.get(Element.Type.NODE, 1)).thenReturn(new ElementGeometry(POS));

		osmNoteQuestDao = mock(OsmNoteQuestDao.class);
		when(osmNoteQuestDao.getAllPositions(any())).thenReturn(Collections.emptyList());

		osmQuestDao = mock(OsmQuestDao.class);
		when(osmQuestDao.getAll(null, null, null, Element.Type.NODE, 1L))
			.thenReturn(Collections.emptyList());

		questType = mock(OsmElementQuestType.class);
		final List<QuestType> questTypes = Collections.singletonList(questType);
		osmQuestUnlocker = new OsmQuestGiver(osmNoteQuestDao, osmQuestDao, elementGeometryDao,
				() -> questTypes);
	}

	@Test public void noteBlocksNewQuests()
	{
		when(questType.isApplicableTo(NODE)).thenReturn(true);
		when(osmNoteQuestDao.getAllPositions(any())).thenReturn(Collections.singletonList(POS));

		assertTrue(osmQuestUnlocker.updateQuests(NODE).createdQuests.isEmpty());
	}

	@Test public void previousQuestBlocksNewQuest()
	{
		OsmQuest q = new OsmQuest(questType, Element.Type.NODE, 1, new ElementGeometry(POS));
		when(osmQuestDao.getAll(null, null, null, Element.Type.NODE, 1L))
			.thenReturn(Collections.singletonList(q));
		when(questType.isApplicableTo(NODE)).thenReturn(true);

		OsmQuestGiver.QuestUpdates r = osmQuestUnlocker.updateQuests(NODE);
		assertTrue(r.createdQuests.isEmpty());
		assertTrue(r.removedQuestIds.isEmpty());
	}

	@Test public void notApplicableBlocksNewQuest()
	{
		when(questType.isApplicableTo(NODE)).thenReturn(false);

		OsmQuestGiver.QuestUpdates r = osmQuestUnlocker.updateQuests(NODE);
		assertTrue(r.createdQuests.isEmpty());
		assertTrue(r.removedQuestIds.isEmpty());
	}

	@Test public void notApplicableRemovesPreviousQuest()
	{
		OsmQuest q = new OsmQuest(123L, questType, Element.Type.NODE, 1, QuestStatus.NEW,
			null, null, new Date(), new ElementGeometry(POS));
		when(osmQuestDao.getAll(null, null, null, Element.Type.NODE, 1L))
			.thenReturn(Collections.singletonList(q));
		when(questType.isApplicableTo(NODE)).thenReturn(false);

		OsmQuestGiver.QuestUpdates r = osmQuestUnlocker.updateQuests(NODE);
		assertTrue(r.createdQuests.isEmpty());
		assertEquals(1,r.removedQuestIds.size());
		assertEquals(123L,(long) r.removedQuestIds.get(0));

		verify(osmQuestDao).deleteAll(Collections.singletonList(123L));
	}

	@Test public void applicableAddsNewQuest()
	{
		when(questType.isApplicableTo(NODE)).thenReturn(true);
		List<OsmQuest> quests = osmQuestUnlocker.updateQuests(NODE).createdQuests;

		assertEquals(1, quests.size());
		OsmQuest quest = quests.get(0);
		assertEquals(1,quest.getElementId());
		assertEquals(Element.Type.NODE,quest.getElementType());
		assertEquals(questType,quest.getType());

		verify(osmQuestDao).deleteAllReverted(Element.Type.NODE, 1);
		verify(osmQuestDao).addAll(Collections.singletonList(quest));
	}
}
