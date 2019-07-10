package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.QuestGroup
import org.junit.Test

import java.util.concurrent.atomic.AtomicBoolean

import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

class OsmQuestChangesetsUploadTest {

	private lateinit var uploader: OsmQuestChangesetsUpload
	private lateinit var osmDao: MapDataDao
	private lateinit var questDB: OsmQuestDao
	private lateinit var elementDB: MergedElementDao
	private lateinit var elementGeometryDB: ElementGeometryDao
	private lateinit var statisticsDB: QuestStatisticsDao
	private lateinit var openChangesetsDB: OpenChangesetsDao
	private lateinit var downloadedTilesDao: DownloadedTilesDao
	private lateinit var singleOsmQuestUpload: SingleOsmQuestUpload
	private lateinit var changesetAutoCloser: ChangesetAutoCloser
	private lateinit var quest: OsmQuest

	@Before fun setUp() {
		val questType = mock(OsmElementQuestType::class.java)
		quest = OsmQuest(1L, questType, Element.Type.NODE, 1, QuestStatus.ANSWERED, null,
			"test case", null, ElementGeometry(OsmLatLon(1.0,2.0)))

		osmDao = mock(MapDataDao::class.java)
		elementDB = mock(MergedElementDao::class.java)
		elementGeometryDB = mock(ElementGeometryDao::class.java)
		statisticsDB = mock(QuestStatisticsDao::class.java)
		openChangesetsDB = mock(OpenChangesetsDao::class.java)
		downloadedTilesDao = mock(DownloadedTilesDao::class.java)
		changesetAutoCloser = mock(ChangesetAutoCloser::class.java)

		questDB = mock(OsmQuestDao::class.java)
		on(questDB.getAll(null, QuestStatus.ANSWERED)).thenReturn(listOf(quest))

        singleOsmQuestUpload = mock(SingleOsmQuestUpload::class.java)

		uploader = OsmQuestChangesetsUpload(
			osmDao, questDB, elementDB, elementGeometryDB, statisticsDB, openChangesetsDB,
			downloadedTilesDao, changesetAutoCloser)
	}

    @Test fun `cancel upload works`() {
	    val dontTouchMe = mock(OsmQuest::class.java)
	    on(dontTouchMe.id).thenThrow(AssertionError())

        on(questDB.getAll(null, QuestStatus.ANSWERED)).thenAnswer {
            Thread.sleep(1000) // take your time...
            listOf(dontTouchMe)
        }

        val cancel = AtomicBoolean(false)
        uploader.setCancelState(cancel)

        val thread = Thread { uploader.upload(...) }
        thread.start()

        cancel.set(true)
        // cancelling the thread works if we come out here without exceptions. If the upload
        // would actually try to start anything, the dontTouchMe quest would throw an AssertionError
	    thread.join()
    }

	@Test fun `handles a changeset conflict`() {
		val oldChangesetId = 42L
		val newChangesetId = 123L

		on(singleOsmQuestUpload.upload(eq(oldChangesetId), any()))
			.thenThrow(OsmConflictException::class.java)

		on(osmDao.openChangeset(any())).thenReturn(newChangesetId)

		on(singleOsmQuestUpload.upload(eq(newChangesetId), any()))
			.thenReturn(OsmQuestUploadResult(true, listOf(), listOf()))

		uploader.upload(...)

		// if it wouldn't work, the mock osmQuestChangeUpload would continue to throw a
		// OsmConflictException

		verify(openChangesetsDB).replace(any(), eq(newChangesetId))
	}

	@Test fun `invalidates area around quest if upload fails`() {
		on(singleOsmQuestUpload.upload(anyLong(), any())).thenReturn(
            OsmQuestUploadResult(false, listOf(), listOf()))

		uploader.upload(...)

		verify(downloadedTilesDao).remove(ArgumentMatchers.any())
	}

	@Test fun `calls visible quest listener for added and removed quests`() {
		val createdQuests = listOf( mock(OsmQuest::class.java))
		val removedQuestIds = listOf(1L, 2L)

		on(singleOsmQuestUpload.upload(anyLong(), any())).thenReturn(
            OsmQuestUploadResult(true, createdQuests, removedQuestIds))

		val listener = mock(VisibleQuestListener::class.java)
		uploader.setVisibleQuestListener(listener)
		uploader.upload(...)

		verify(listener).onQuestsCreated(createdQuests, QuestGroup.OSM)
		verify(listener).onQuestsRemoved(removedQuestIds, QuestGroup.OSM)
	}

	@Test fun `calls progress listener for successfully uploaded quest`() {
		on(singleOsmQuestUpload.upload(anyLong(), any())).thenReturn(
            OsmQuestUploadResult(true, listOf(), listOf()))

		val listener = mock(OnUploadedChangeListener::class.java)
		uploader.setProgressListener(listener)
		uploader.upload(...)

		verify(listener).onUploaded()
	}

	@Test fun `calls progress listener for dropped quest`() {
		on(singleOsmQuestUpload.upload(anyLong(), any())).thenReturn(
            OsmQuestUploadResult(false, listOf(), listOf()))

		val listener = mock(OnUploadedChangeListener::class.java)
		uploader.setProgressListener(listener)
		uploader.upload(...)

		verify(listener).onDiscarded()
	}
}
