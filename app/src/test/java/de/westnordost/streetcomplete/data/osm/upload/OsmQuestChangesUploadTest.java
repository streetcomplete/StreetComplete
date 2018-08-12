package de.westnordost.streetcomplete.data.osm.upload;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import de.westnordost.osmapi.changesets.ChangesetInfo;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.common.errors.OsmConflictException;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.user.User;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetKey;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.AOsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

import static org.mockito.Mockito.*;

public class OsmQuestChangesUploadTest extends TestCase
{

	private static long A_NODE_ID = 5;

	public void testCancel() throws InterruptedException
	{
		OsmQuestDao questDb = mock(OsmQuestDao.class);
		ElementGeometryDao elementGeometryDao = mock(ElementGeometryDao.class);
		MergedElementDao elementDB = mock(MergedElementDao.class);
		OpenChangesetsDao openChangesetsDb = mock(OpenChangesetsDao.class);
		when(questDb.getAll(null, QuestStatus.ANSWERED)).thenAnswer(invocation ->
		{
			Thread.sleep(1000); // take your time...
			ArrayList<OsmQuest> result = new ArrayList<>();
			result.add(null);
			return result;
		});

		final OsmQuestChangesUpload u = new OsmQuestChangesUpload(null, questDb, elementDB,
				elementGeometryDao, null, openChangesetsDb, null, null, null, null);
		final AtomicBoolean cancel = new AtomicBoolean(false);

		Thread t = new Thread(() -> u.upload(cancel));
		t.start();

		cancel.set(true);
		// cancelling the thread works if we come out here without exceptions. If the upload
		// would actually try to start anything, there would be a nullpointer exception since we
		// feeded it only with nulls to work with
		t.join();

		// this is not called anymore immediately since quests are kept in DB for some time before deletion (#373)
		//verify(elementGeometryDao).deleteUnreferenced();
		//verify(elementDB).deleteUnreferenced();
	}

	public void testDropChangeWhenElementDeleted()
	{
		OsmQuest quest = createAnsweredQuest(null);
		OsmQuestDao questDb = mock(OsmQuestDao.class);
		DownloadedTilesDao downloadedTilesDao = mock(DownloadedTilesDao.class);
		OsmQuestChangesUpload u = new OsmQuestChangesUpload(null, questDb, null, null, null, null,
				null, downloadedTilesDao, null, null);

		assertFalse(u.uploadQuestChange(-1, quest, null, false, false));

		verify(downloadedTilesDao).remove(any(Point.class));
		verify(questDb).delete(quest.getId());
	}

	public void testDropChangeWhenUnresolvableElementChange()
	{
		OsmQuest quest = createAnsweredQuestWithNonAppliableChange();
		Element element = createElement();

		OsmQuestDao questDb = mock(OsmQuestDao.class);
		MergedElementDao elementDao = mock(MergedElementDao.class);
		DownloadedTilesDao downloadedTilesDao = mock(DownloadedTilesDao.class);
		OsmQuestChangesUpload u = new OsmQuestChangesUpload(null, questDb, elementDao, null, null, null,
				null, downloadedTilesDao, null, null);
		assertFalse(u.uploadQuestChange(123, quest, element, false, false));
		verify(questDb).delete(quest.getId());
		verify(downloadedTilesDao).remove(any(Point.class));
	}

	/* Simulates an element conflict while uploading the element, when updating the element from
	   mock server, it turns out that it has been deleted */
	public void testHandleElementConflictAndThenDeleted()
	{
		final long changesetId = 123;
		final long userId = 10;

		OsmQuest quest = createAnsweredQuestWithAppliableChange();
		Element element = createElement();

		MergedElementDao elementDb = mock(MergedElementDao.class);
		OsmQuestDao questDb = mock(OsmQuestDao.class);
		DownloadedTilesDao downloadedTilesDao = mock(DownloadedTilesDao.class);

		MapDataDao mapDataDao = createMapDataDaoThatReportsConflictOnUploadAndNodeDeleted();

		// a changeset dao+prefs that report that the changeset is open and the changeset is owned by the user
		ChangesetsDao changesetsDao = mock(ChangesetsDao.class);
		when(changesetsDao.get(changesetId)).thenReturn(createOpenChangesetForUser(userId));
		SharedPreferences prefs = mock(SharedPreferences.class);
		when(prefs.getLong(Prefs.OSM_USER_ID, -1)).thenReturn(userId);

		OsmQuestChangesUpload u = new OsmQuestChangesUpload(mapDataDao, questDb, elementDb, null,
				null, null, changesetsDao, downloadedTilesDao, prefs, null);

		assertFalse(u.uploadQuestChange(changesetId, quest, element, false, false));
		verify(questDb).delete(quest.getId());
		verify(elementDb).delete(Element.Type.NODE, A_NODE_ID);
		verify(downloadedTilesDao).remove(any(Point.class));
	}

	/* Simulates the changeset that is about to be used was created by a different user, so a new
	*  changeset needs to be created. (after that, it runs into the same case above, for simplicity
	*  sake*/
	public void testHandleChangesetConflictDifferentUser()
	{
		final long userId = 10;
		final long otherUserId = 15;
		final long firstChangesetId = 123;
		final long secondChangesetId = 124;
		// reports that the changeset is open but does belong to another user
		ChangesetsDao changesetsDao = mock(ChangesetsDao.class);
		when(changesetsDao.get(firstChangesetId)).thenReturn(createOpenChangesetForUser(otherUserId));
		when(changesetsDao.get(secondChangesetId)).thenReturn(createOpenChangesetForUser(userId));

		doTestHandleChangesetConflict(changesetsDao, userId, firstChangesetId, secondChangesetId);
	}

	/* Simulates the changeset that is about to be used is already closed, so a new changeset needs
	to be created. (after that, it runs into the same case above, for simplicity sake*/
	public void testHandleChangesetConflictAlreadyClosed()
	{
		final long userId = 10;
		final long firstChangesetId = 123;
		final long secondChangesetId = 124;
		// reports that the changeset is open but does belong to another user
		ChangesetsDao changesetsDao = mock(ChangesetsDao.class);
		when(changesetsDao.get(firstChangesetId)).thenReturn(createClosedChangesetForUser(userId));
		when(changesetsDao.get(secondChangesetId)).thenReturn(createOpenChangesetForUser(userId));

		doTestHandleChangesetConflict(changesetsDao, userId, firstChangesetId, secondChangesetId);
	}

	private void doTestHandleChangesetConflict(ChangesetsDao changesetsDao, long userId,
											   long firstChangesetId, long secondChangesetId)
	{
		OsmQuest quest = createAnsweredQuestWithAppliableChange();
		Element element = createElement();

		MergedElementDao elementDb = mock(MergedElementDao.class);
		OsmQuestDao questDb = mock(OsmQuestDao.class);
		DownloadedTilesDao downloadedTilesDao = mock(DownloadedTilesDao.class);

		OpenChangesetsDao manageChangesetsDb = mock(OpenChangesetsDao.class);

		MapDataDao mapDataDao = createMapDataDaoThatReportsConflictOnUploadAndNodeDeleted();
		when(mapDataDao.openChangeset(any(Map.class))).thenReturn(secondChangesetId);

		SharedPreferences prefs = createPreferencesForUser(userId);

		OsmQuestChangesUpload u = new OsmQuestChangesUpload(mapDataDao, questDb, elementDb, null,
				null, manageChangesetsDb, changesetsDao, downloadedTilesDao, prefs, null);

		assertFalse(u.uploadQuestChange(firstChangesetId, quest, element, false, false));

		verify(manageChangesetsDb).replace(new OpenChangesetKey("TestQuestType","test case"), secondChangesetId);
		verify(questDb).delete(quest.getId());
		verify(elementDb).delete(Element.Type.NODE, A_NODE_ID);
		verify(downloadedTilesDao).remove(any(Point.class));
	}

	private static SharedPreferences createPreferencesForUser(long userId)
	{
		SharedPreferences prefs = mock(SharedPreferences.class);
		when(prefs.getLong(Prefs.OSM_USER_ID, -1)).thenReturn(userId);
		return prefs;
	}

	// this map data dao ensures that the program is running into a conflict and finally into
	// an unresolvable one for the node (because it has been deleted).
	private static MapDataDao createMapDataDaoThatReportsConflictOnUploadAndNodeDeleted()
	{
		MapDataDao mapDataDao = mock(MapDataDao.class);
		doThrow(OsmConflictException.class).when(mapDataDao)
				.uploadChanges(any(Long.class), any(Iterable.class), any(Handler.class));
		when(mapDataDao.getNode(A_NODE_ID)).thenReturn(null);
		return mapDataDao;
	}

	private static ChangesetInfo createOpenChangesetForUser(long id)
	{
		ChangesetInfo result = createChangesetForUser(id);
		result.isOpen = true;
		return result;
	}

	private static ChangesetInfo createClosedChangesetForUser(long id)
	{
		ChangesetInfo result = createChangesetForUser(id);
		result.isOpen = false;
		return result;
	}

	private static ChangesetInfo createChangesetForUser(long id)
	{
		ChangesetInfo result = new ChangesetInfo();
		result.user = new User(id, "Hans Wurst");
		return result;
	}

	public void testUploadNormally()
	{
		OsmQuest quest = createAnsweredQuestWithAppliableChange();
		Element element = createElement();

		OsmQuestDao questDb = mock(OsmQuestDao.class);
		MapDataDao mapDataDao = mock(MapDataDao.class);
		QuestStatisticsDao statisticsDao = mock(QuestStatisticsDao.class);
		MergedElementDao elementDb = mock(MergedElementDao.class);
		OsmQuestGiver osmQuestUnlocker = mock(OsmQuestGiver.class);
		when(osmQuestUnlocker.updateQuests(any(Element.class))).thenReturn(new OsmQuestGiver.QuestUpdates());
		OsmQuestChangesUpload u = new OsmQuestChangesUpload(mapDataDao, questDb, elementDb, null,
				statisticsDao, null, null, null, null, osmQuestUnlocker);

		assertTrue(u.uploadQuestChange(1, quest, element, false, false));
		assertEquals(QuestStatus.CLOSED, quest.getStatus());
		verify(statisticsDao).addOne("TestQuestType");
	}

	private static class TestQuestType extends AOsmElementQuestType
	{
		@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes) { }
		@Override public String getCommitMessage() { return null; }
		@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
		{
			return true;
		}
		@Override public AbstractQuestAnswerFragment createForm() { return null; }
		@Override public int getIcon() { return 0; }
		@Override public int getTitle(@NonNull Map<String,String> tags) { return 0; }
		@Nullable @Override public Boolean isApplicableTo(Element element) { return false; }
	}

	private static OsmQuest createAnsweredQuestWithAppliableChange()
	{
		StringMapEntryChange aPossibleChange = new StringMapEntryAdd("somekey","value");
		StringMapChanges changes = new StringMapChanges(Collections.singletonList(aPossibleChange));
		return createAnsweredQuest(changes);
	}

	private static OsmQuest createAnsweredQuestWithNonAppliableChange()
	{
		StringMapEntryChange nonPossibleChange = new StringMapEntryDelete("somekey","value");
		StringMapChanges changes = new StringMapChanges(Collections.singletonList(nonPossibleChange));
		return createAnsweredQuest(changes);
	}

	private static OsmQuest createAnsweredQuest(StringMapChanges changes)
	{
		return new OsmQuest(3L, new TestQuestType(), Element.Type.NODE, A_NODE_ID,
				QuestStatus.ANSWERED, changes, "test case", null, createElementGeometry());
	}

	private static ElementGeometry createElementGeometry()
	{
		return new ElementGeometry(new OsmLatLon(1,2));
	}

	private static Element createElement()
	{
		return new OsmNode(A_NODE_ID, 0, new OsmLatLon(1,2), new HashMap<>());
	}
}
