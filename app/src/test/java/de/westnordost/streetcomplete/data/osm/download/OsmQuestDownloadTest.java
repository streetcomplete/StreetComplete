package de.westnordost.streetcomplete.data.osm.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

import de.westnordost.countryboundaries.CountryBoundaries;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.osm.Countries;
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
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OsmQuestDownloadTest
{
	private ElementGeometryDao geometryDb;
	private MergedElementDao elementDb;
	private OsmQuestDao osmQuestDao;
	private FutureTask<CountryBoundaries> countryBoundariesFuture;

	@Before public void setUp()
	{
		geometryDb = mock(ElementGeometryDao.class);
		elementDb = mock(MergedElementDao.class);
		osmQuestDao = mock(OsmQuestDao.class);
		countryBoundariesFuture = mock(FutureTask.class);
	}

	@Test public void ignoreBlacklistedPositionsAndInvalidGeometry()
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

		OsmQuestDownload dl = new OsmQuestDownload(geometryDb, elementDb, osmQuestDao, countryBoundariesFuture);

		VisibleQuestListener listener = mock(VisibleQuestListener.class);
		dl.setQuestListener(listener);

		dl.download(questType, new BoundingBox(0,0,1,1), Collections.singleton(blacklistPos));

		verify(listener, times(0)).onQuestsCreated(any(), any());
	}

	@Test public void deleteObsoleteQuests()
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

		when(osmQuestDao.getAll(any(), any(), any(), any(), any())).thenReturn(quests);

		doAnswer(invocation ->
		{
			Collection<Long> deletedQuests = (Collection<Long>) (invocation.getArguments()[0]);
			assertEquals(1, deletedQuests.size());
			assertEquals(13L, (long) deletedQuests.iterator().next());
			return 1;
		}).when(osmQuestDao).deleteAll(any());

		OsmQuestDownload dl = new OsmQuestDownload(geometryDb, elementDb, osmQuestDao, countryBoundariesFuture);

		VisibleQuestListener listener = mock(VisibleQuestListener.class);
		dl.setQuestListener(listener);

		// -> we expect that quest with node #5 is removed
		dl.download(questType, new BoundingBox(0,0,1,1), Collections.emptySet());

		verify(osmQuestDao).deleteAll(any());
		verify(listener).onQuestsRemoved(any(), any());
	}


	private void setUpOsmQuestDaoMockWithNoPreviousElements()
	{
		when(osmQuestDao.getAll(any(), any(), any(),any(), any()))
				.thenReturn(Collections.emptyList());
	}

	private static class ElementWithGeometry
	{
		Element element;
		ElementGeometry geometry;
	}

	private static class ListBackedQuestType implements OsmElementQuestType<String>
	{
		private final List<ElementWithGeometry> list;

		public ListBackedQuestType(List<ElementWithGeometry> list)
		{
			this.list = list;
		}

		@NonNull @Override public AbstractQuestAnswerFragment<String> createForm() {
			return new AbstractQuestAnswerFragment<String>() {}; }
		@Override public int getIcon() { return 0; }
		@Override public int getTitle(@NonNull Map<String,String> tags) { return 0; }
		@Override public void applyAnswerTo(@NonNull String answer, @NonNull StringMapChangesBuilder changes) {}
		@Override @NonNull public String getCommitMessage() { return ""; }
		@Nullable @Override public Boolean isApplicableTo(@NonNull Element element) { return false; }

		@Override public boolean download(@NonNull BoundingBox bbox, @NonNull
			MapDataWithGeometryHandler handler)
		{
			for (ElementWithGeometry e : list)
			{
				handler.handle(e.element, e.geometry);
			}
			return true;
		}

		@NotNull @Override public Countries getEnabledForCountries() { return Countries.ALL; }
		@Override public boolean getHasMarkersAtEnds() { return false; }
		@Override public int getTitle() { return 0; }
		@Override public void cleanMetadata() {}
		@Override public int getDefaultDisabledMessage() { return 0; }
	}
}
