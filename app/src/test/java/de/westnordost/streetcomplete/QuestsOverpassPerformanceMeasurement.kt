package de.westnordost.streetcomplete

import de.westnordost.osmapi.ApiRequestWriter
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.download.*
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType
import de.westnordost.streetcomplete.quests.QuestModule
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsDao
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao
import org.mockito.Mockito.mock
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.URLEncoder
import java.util.concurrent.FutureTask
import kotlin.system.measureTimeMillis

fun main() {

    val overpassMapDataDao = TestOverpassMapDataDao()

    val overpassMock = mock(OverpassMapDataDao::class.java)
    on(overpassMock.getAndHandleQuota(any(), any())).then { invocation ->
        overpassMapDataDao.get(invocation.getArgument(0) as String)
        true
    }

    val registry = QuestModule.questTypeRegistry(
        mock(OsmNoteQuestType::class.java),
        overpassMock,
        mock(RoadNameSuggestionsDao::class.java),
        mock(PutRoadNameSuggestionsHandler::class.java),
        mock(TrafficFlowSegmentsDao::class.java),
        mock(WayTrafficFlowDao::class.java),
        mock(FutureTask::class.java) as FutureTask<FeatureDictionary>?
    )

    val hamburg = BoundingBox(53.5, 9.9, 53.6, 10.0)

    for (questType in registry.all) {
        if (questType is OsmElementQuestType) {
            print(questType.javaClass.simpleName + ": ")
            questType.download(hamburg, mock(MapDataWithGeometryHandler::class.java))
            println()
        }
    }
    println("Total response time: ${overpassMapDataDao.totalTime/1000}s")
    println("Total waiting time: ${overpassMapDataDao.totalWaitTime}s")
}

private class TestOverpassMapDataDao {

    var totalTime = 0L
    var totalWaitTime = 0L

    val osm = OsmConnection(
            "https://overpass.maptime.in/api/",
            "StreetComplete Overpass Query Performance Test",
            null,
            (180 + 4) * 1000)

    fun get(query: String) {
        var time: Long
        while(true) {
            try {
                time = measureTimeMillis {
                    osm.makeRequest("interpreter", "POST", false, getWriter(query), null)
                }
                break
            } catch (e: OsmApiException) {
                if (e.errorCode == 429) {
                    val status = getStatus()
                    if (status.availableSlots == 0) {
                        val waitInSeconds = status.nextAvailableSlotIn ?: 60
                        totalWaitTime += waitInSeconds
                        sleep(waitInSeconds * 1000L)
                    }
                    continue
                } else throw e
            }
        }
        totalTime += time
        val s = "%.1f".format(time/1000.0)
        print("${s}s ")
    }

    private fun getStatus(): OverpassStatus = osm.makeRequest("status", OverpassStatusParser())

    private fun getWriter(query: String) = object : ApiRequestWriter {
        override fun write(out: OutputStream) {
            val utf8 = Charsets.UTF_8
            val request = "data=" + URLEncoder.encode(query, utf8.name())
            out.write(request.toByteArray(utf8))
        }
        override fun getContentType() = "application/x-www-form-urlencoded"
    }
}

