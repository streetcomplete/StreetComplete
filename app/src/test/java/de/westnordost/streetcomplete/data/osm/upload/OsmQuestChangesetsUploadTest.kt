package de.westnordost.streetcomplete.data.osm.upload

import de.westnordost.streetcomplete.data.QuestGroup
import org.junit.Test

import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

class OsmQuestChangesetsUploadTest {

	private lateinit var elementDB: MergedElementDao
	private lateinit var elementGeometryDB: ElementGeometryDao
	private lateinit var statisticsDB: QuestStatisticsDao
	private lateinit var downloadedTilesDao: DownloadedTilesDao
/*
TODO

	@Before fun setUp() {
		elementDB = mock(MergedElementDao::class.java)
		elementGeometryDB = mock(ElementGeometryDao::class.java)
		statisticsDB = mock(QuestStatisticsDao::class.java)
		downloadedTilesDao = mock(DownloadedTilesDao::class.java)

		uploader = OsmQuestChangesetsUpload(elementDB, elementGeometryDB, statisticsDB, downloadedTilesDao)
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
	*/
}
