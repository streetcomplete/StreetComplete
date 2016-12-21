package de.westnordost.streetcomplete.data.osm.download;

import android.graphics.Rect;

import junit.framework.TestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
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
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OsmQuestDownloadTest extends TestCase
{
	private OverpassQuestType questType1;

	private OverpassMapDataDao overpassServer;
	private ElementGeometryDao geometryDb;
	private MergedElementDao elementDb;
	private DownloadedTilesDao downloadedTilesDao;
	private OsmQuestDao osmQuestDao;

	@Override public void setUp()
	{
		geometryDb = mock(ElementGeometryDao.class);
		elementDb = mock(MergedElementDao.class);
		downloadedTilesDao = mock(DownloadedTilesDao.class);
		osmQuestDao = mock(OsmQuestDao.class);
		overpassServer = mock(OverpassMapDataDao.class);
		questType1 = mock(OverpassQuestType.class);
	}

	public void testHandleOverpassQuota() throws InterruptedException
	{
		doThrow(OsmTooManyRequestsException.class).
				when(overpassServer).get(anyString(), any(MapDataWithGeometryHandler.class));

		OverpassStatus status = new OverpassStatus();
		status.availableSlots = 0;
		status.nextAvailableSlotIn = 2;
		when(overpassServer.getStatus()).thenReturn(status);

		setUpOsmQuestDaoMockWithNoPreviousElements();

		final OsmQuestDownload dl = new OsmQuestDownload(overpassServer, null, null, osmQuestDao, null);

		// the downloader will call get() on the dao, get an exception in return, ask its status
		// then and at least wait for the specified amount of time before calling again
		Thread dlThread = new Thread()
		{
			@Override public void run()
			{
				assertEquals(0,dl.download(questType1, new Rect(0,0,10,10), null));
			}
		};
		dlThread.start();

		// sleep the wait time: Downloader should not try to call
		// overpass again in this time
		Thread.sleep(status.nextAvailableSlotIn * 1000);
		verify(overpassServer, times(1)).get(anyString(), any(MapDataWithGeometryHandler.class));
		verify(overpassServer, times(1)).getStatus();

		// now we test if downloader will call overpass again after that time. It is not really
		// defined when the downloader must call overpass again, lets assume 1.5 secs here and
		// change it when it fails
		Thread.sleep(1500);
		verify(overpassServer, times(2)).get(anyString(), any(MapDataWithGeometryHandler.class));

		// we are done here, interrupt thread (still part of the test though...)
		dlThread.interrupt();
		dlThread.join();
	}

	public void testIgnoreBlacklistedPositions()
	{
		LatLon blacklistPos = new OsmLatLon(3.0,4.0);

		OverpassMapDataDao overpassServer = new TestListBackedOverpassDao(Collections.singletonList(
				new ElementWithGeometry(
						new OsmNode(0,0,blacklistPos,null,null),
						new ElementGeometry(blacklistPos))));
		setUpOsmQuestDaoMockWithNoPreviousElements();

		OsmQuestDownload dl = new OsmQuestDownload(overpassServer, geometryDb, elementDb, osmQuestDao, downloadedTilesDao);

		VisibleQuestListener listener = mock(VisibleQuestListener.class);
		dl.setQuestListener(listener);

		OverpassQuestType appliesToAnything = mock(OverpassQuestType.class);
		when(appliesToAnything.appliesTo(any(Element.class))).thenReturn(true);
		assertEquals(0,dl.download(appliesToAnything, new Rect(0,0,1,1), Collections.singleton(blacklistPos)));

		verify(listener, times(0)).onQuestsCreated(any(Collection.class), any(QuestGroup.class));
	}

	public void testDeleteObsoleteQuests()
	{
		OverpassQuestType appliesToAnything = mock(OverpassQuestType.class);
		when(appliesToAnything.appliesTo(any(Element.class))).thenReturn(true);

		LatLon pos = new OsmLatLon(3.0,4.0);

		// overpass mock will only "find" the Node #4
		List<ElementWithGeometry> elements = new ArrayList<>();
		elements.add(new ElementWithGeometry(
				new OsmNode(4,0,pos,null,null), new ElementGeometry(pos)));
		OverpassMapDataDao overpassServer = new TestListBackedOverpassDao(elements);

		// in the quest database mock, there are quests for node 4 and node 5
		List<OsmQuest> quests = new ArrayList<>();
		quests.add(new OsmQuest(
				12L, appliesToAnything, Element.Type.NODE, 4, QuestStatus.NEW, null, null,
				new Date(), new ElementGeometry(pos)));
		quests.add(new OsmQuest(
				13L, appliesToAnything, Element.Type.NODE, 5, QuestStatus.NEW, null, null,
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

		OsmQuestDownload dl = new OsmQuestDownload(overpassServer, geometryDb, elementDb, osmQuestDao, downloadedTilesDao);

		VisibleQuestListener listener = mock(VisibleQuestListener.class);
		dl.setQuestListener(listener);

		// -> we expect that quest with node #5 is removed
		dl.download(appliesToAnything, new Rect(0,0,1,1), null);

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

	private class TestListBackedOverpassDao extends OverpassMapDataDao
	{
		private List<ElementWithGeometry> elements;

		public TestListBackedOverpassDao(List<ElementWithGeometry> elements)
		{
			super(null, null);
			this.elements = elements;
		}

		@Override public void get(String oql, MapDataWithGeometryHandler handler)
		{
			for(ElementWithGeometry e : elements)
			{
				handler.handle(e.element, e.geometry);
			}
		}
	}

	private class ElementWithGeometry
	{
		ElementWithGeometry(Element element, ElementGeometry geometry) {
			this.element = element;
			this.geometry = geometry;
		}
		Element element;
		ElementGeometry geometry;
	}
}
