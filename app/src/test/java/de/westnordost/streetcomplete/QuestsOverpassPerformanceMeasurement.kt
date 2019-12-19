package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.osmapi.overpass.OverpassStatus
import de.westnordost.osmapi.overpass.OverpassStatusParser
import de.westnordost.osmapi.ApiRequestWriter
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.quests.QuestModule
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.URLEncoder
import kotlin.system.measureTimeMillis

fun main() {

    val overpassMapDataDao = TestOverpassMapDataDao()

    val overpassMock: OverpassMapDataAndGeometryDao = mock()
    on(overpassMock.query(any(), any())).then { invocation ->
        overpassMapDataDao.get(invocation.getArgument(0) as String)
        true
    }

    val registry = QuestModule.questTypeRegistry(mock(), overpassMock, mock(), mock(), mock(), mock())

    val hamburg = BoundingBox(53.5, 9.9, 53.6, 10.0)

    for (questType in registry.all) {
        if (questType is OsmElementQuestType) {
            print(questType.javaClass.simpleName + ": ")
            questType.download(hamburg, mock())
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

