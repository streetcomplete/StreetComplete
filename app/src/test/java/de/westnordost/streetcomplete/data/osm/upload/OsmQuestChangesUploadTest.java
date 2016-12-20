package de.westnordost.streetcomplete.data.osm.upload;

import android.os.Bundle;

import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.changes.DiffElement;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

import static org.mockito.Mockito.*;

public class OsmQuestChangesUploadTest extends TestCase
{

	public void testCancel() throws InterruptedException
	{
		OsmQuestDao questDb = mock(OsmQuestDao.class);
		ElementGeometryDao elementGeometryDao = mock(ElementGeometryDao.class);
		MergedElementDao elementDB = mock(MergedElementDao.class);
		when(questDb.getAll(null, QuestStatus.ANSWERED)).thenAnswer(
				new Answer<List<OsmQuest>>()
				{
					@Override public List<OsmQuest> answer(InvocationOnMock invocation) throws Throwable
					{
						Thread.sleep(1000); // take your time...
						ArrayList<OsmQuest> result = new ArrayList<>();
						result.add(null);
						return result;
					}
				});

		final OsmQuestChangesUpload u = new OsmQuestChangesUpload(null, questDb, elementDB, elementGeometryDao, null);
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
		// cancelling the thread works if we come out here without exceptions. If the upload
		// would actually try to start anything, there would be a nullpointer exception since we
		// feeded it only with nulls to work with
		t.join();

		verify(elementGeometryDao).deleteUnreferenced();
		verify(elementDB).deleteUnreferenced();
	}

	public void testDropChangeWhenElementDeleted()
	{
		OsmQuest quest = createQuest(null);
		OsmQuestDao questDb = mock(OsmQuestDao.class);
		OsmQuestChangesUpload u = new OsmQuestChangesUpload(null, questDb, null, null, null);

		assertFalse(u.uploadQuestChanges(quest, null, false));

		verify(questDb).delete(quest.getId());
	}

	public void testDropChangeWhenUnresolvableConflict()
	{
		StringMapEntryChange nonPossibleChange = new StringMapEntryDelete("somekey","value");

		StringMapChanges changes = new StringMapChanges(Collections.singletonList(nonPossibleChange));
		OsmQuest quest = createQuest(changes);
		Element element = createElement();

		OsmQuestDao questDb = mock(OsmQuestDao.class);
		OsmQuestChangesUpload u = new OsmQuestChangesUpload(null, questDb, null, null, null);
		assertFalse(u.uploadQuestChanges(quest, element, false));
		verify(questDb).delete(quest.getId());
	}

	public void testHandleConflictAndThenDeleted()
	{
		StringMapEntryChange aPossibleChange = new StringMapEntryAdd("somekey","value");
		StringMapChanges changes = new StringMapChanges(Collections.singletonList(aPossibleChange));
		OsmQuest quest = createQuest(changes);
		Element element = createElement();

		MergedElementDao elementDb = mock(MergedElementDao.class);
		OsmQuestDao questDb = mock(OsmQuestDao.class);
		MapDataDao mapDataDao = mock(MapDataDao.class);
		when(mapDataDao.updateMap(any(Map.class), any(Iterable.class), isNull(Handler.class)))
				.thenThrow(OsmConflictException.class);
		when(mapDataDao.getNode(5)).thenReturn(null);
		OsmQuestChangesUpload u = new OsmQuestChangesUpload(mapDataDao, questDb, elementDb, null, null);

		assertFalse(u.uploadQuestChanges(quest, element, false));
		verify(questDb).delete(quest.getId());
		verify(elementDb).delete(Element.Type.NODE, 5);
	}

	public void testUploadNormally()
	{
		StringMapEntryChange aPossibleChange = new StringMapEntryAdd("somekey","value");
		StringMapChanges changes = new StringMapChanges(Collections.singletonList(aPossibleChange));
		OsmQuest quest = createQuest(changes);
		Element element = createElement();

		OsmQuestDao questDb = mock(OsmQuestDao.class);
		MapDataDao mapDataDao = new TestMapDataDao(element);
		QuestStatisticsDao statisticsDao = mock(QuestStatisticsDao.class);
		OsmQuestChangesUpload u = new OsmQuestChangesUpload(mapDataDao, questDb, null, null, statisticsDao);

		assertTrue(u.uploadQuestChanges(quest, element, false));
		verify(questDb).delete(quest.getId());
		verify(statisticsDao).addOne("TestQuestType");
	}

	private class TestMapDataDao extends MapDataDao
	{
		Element element;

		public TestMapDataDao(Element element)
		{
			super(null);
			this.element = element;
		}

		@Override public long updateMap(Map<String, String> tags, Iterable<Element> elements,
							  Handler<DiffElement> handler)
		{
			assertEquals(tags.get("created_by"), ApplicationConstants.USER_AGENT);
			assertEquals(tags.get("comment"), "commit message");
			assertEquals(tags.get("source"), "survey");
			assertEquals(tags.get("StreetComplete:quest_type"), "TestQuestType");

			assertEquals(elements.iterator().next(), element);
			return 0;
		}
	}

	private class TestQuestType extends OverpassQuestType
	{
		@Override protected String getTagFilters() { return "nodes"; }
		@Override public Integer applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) { return null; }
		@Override public int importance() { return 0; }
		@Override public AbstractQuestAnswerFragment getForm() { return null; }
		@Override public String getIconName() { return null; }
	}

	private OsmQuest createQuest(StringMapChanges changes)
	{
		return new OsmQuest(3L, new TestQuestType(), Element.Type.NODE, 5,
				QuestStatus.ANSWERED, changes, "commit message", null, null);
	}

	private Element createElement()
	{
		return new OsmNode(5, 0, new OsmLatLon(1,2), new HashMap<String,String>(), null);
	}
}
