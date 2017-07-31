package de.westnordost.streetcomplete.data.osm.download;

import android.os.Bundle;

import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.streetcomplete.data.QuestGroup;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OsmQuestDownloadTest extends TestCase
{
	private ElementGeometryDao geometryDb;
	private MergedElementDao elementDb;
	private OsmQuestDao osmQuestDao;

	@Override public void setUp()
	{
		geometryDb = mock(ElementGeometryDao.class);
		elementDb = mock(MergedElementDao.class);
		osmQuestDao = mock(OsmQuestDao.class);
	}

	public void testIgnoreBlacklistedPositionsAndInvalidGeometry()
	{
		LatLon blacklistPos = new OsmLatLon(3.0,4.0);

		ElementWithGeometry blacklistElement = new ElementWithGeometry();
		blacklistElement.element = new OsmNode(0,0,blacklistPos,null);
		blacklistElement.geometry = new ElementGeometry(blacklistPos);
		ElementWithGeometry invalidGeometryElement = new ElementWithGeometry();
		invalidGeometryElement.element = new OsmNode(0,0,new OsmLatLon(1.0,1.0),null);
		invalidGeometryElement.geometry = null;

		OsmElementQuestType questType = new ListBackedQuestType(
				Arrays.asList(blacklistElement, invalidGeometryElement));

		setUpOsmQuestDaoMockWithNoPreviousElements();

		OsmQuestDownload dl = new OsmQuestDownload(geometryDb, elementDb, osmQuestDao);

		VisibleQuestListener listener = mock(VisibleQuestListener.class);
		dl.setQuestListener(listener);

		dl.download(questType, new BoundingBox(0,0,1,1), Collections.singleton(blacklistPos));

		verify(listener, times(0)).onQuestsCreated(any(Collection.class), any(QuestGroup.class));
	}

	public void testDeleteObsoleteQuests()
	{
		LatLon pos = new OsmLatLon(3.0,4.0);

		ElementWithGeometry node4 = new ElementWithGeometry();
		node4.element = new OsmNode(4,0,pos,null);
		node4.geometry =  new ElementGeometry(pos);
		// questType mock will only "find" the Node #4
		OsmElementQuestType questType = new ListBackedQuestType(Collections.singletonList(node4));

		// in the quest database mock, there are quests for node 4 and node 5
		List<OsmQuest> quests = new ArrayList<>();
		quests.add(new OsmQuest(
				12L, questType, Element.Type.NODE, 4, QuestStatus.NEW, null, null,
				new Date(), new ElementGeometry(pos)));
		quests.add(new OsmQuest(
				13L, questType, Element.Type.NODE, 5, QuestStatus.NEW, null, null,
				new Date(), new ElementGeometry(pos)));
		when(osmQuestDao.getAll(
				any(BoundingBox.class), any(QuestStatus.class), anyString(),
				any(Element.Type.class), anyLong()))
				.thenReturn(quests);
		doAnswer(new Answer<Integer>()
		{
			@Override public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				Collection<Long> deletedQuests = (Collection<Long>) (invocation.getArguments()[0]);
				assertEquals(1, deletedQuests.size());
				assertEquals(13L, (long) deletedQuests.iterator().next());
				return 1;
			}
		}).when(osmQuestDao).deleteAll(any(Collection.class));

		OsmQuestDownload dl = new OsmQuestDownload(geometryDb, elementDb, osmQuestDao);

		VisibleQuestListener listener = mock(VisibleQuestListener.class);
		dl.setQuestListener(listener);

		// -> we expect that quest with node #5 is removed
		dl.download(questType, new BoundingBox(0,0,1,1), null);

		verify(osmQuestDao).deleteAll(any(Collection.class));
		verify(listener).onQuestsRemoved(any(Collection.class), any(QuestGroup.class));
	}


	private void setUpOsmQuestDaoMockWithNoPreviousElements()
	{
		when(osmQuestDao.getAll(
				any(BoundingBox.class), any(QuestStatus.class), anyString(),
				any(Element.Type.class), anyLong()))
				.thenReturn(Collections.<OsmQuest>emptyList());
	}

	private class ElementWithGeometry
	{
		Element element;
		ElementGeometry geometry;
	}

	private class ListBackedQuestType implements OsmElementQuestType
	{
		private final List<ElementWithGeometry> list;

		public ListBackedQuestType(List<ElementWithGeometry> list)
		{
			this.list = list;
		}

		@Override public AbstractQuestAnswerFragment createForm() { return null; }
		@Override public String getIconName() { return null; }
		@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) {}
		@Override public String getCommitMessage() { return null; }
		@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
		{
			for (ElementWithGeometry e : list)
			{
				handler.handle(e.element, e.geometry);
			}
			return true;
		}
	}
}
