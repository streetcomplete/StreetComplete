package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/** Takes care of downloading ATP data into persistent storage */
class AtpDownloader(
    private val atpApi: AtpApiClient,
    private val atpController: AtpController
) {
    suspend fun download(bbox: BoundingBox) {
        val time = nowAsEpochMilliseconds()

        //50.071980/20.037185 Plac Centralny fake entry
        //val entries = listOf(mapOf("lat" to 50.071980, "lon" to 20.037185))
        val entries = listOf(AtpEntry(
            position = LatLon(
                latitude = 50.071980,
                longitude = 20.037185
            ),
            id = 1,
            osmElementMatchId = null,
            osmElementMatchType = null,
            tagsInATP = mapOf( "shop" to "convenience", "name" to "fake entry"),
            tagsInOSM = null
        ))
        /*
        val entries = notesApi // TODO look at notesApi, create ATP API
            .getAllOpen(bbox, 10000)
            // exclude invalid notes (#1338)
            .filter { it.comments.isNotEmpty() }
        */
        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Downloaded ${entries.size} ATP entries in ${seconds.format(1)}s")

        yield()

        withContext(Dispatchers.IO) { atpController.putAllForBBox(bbox, entries) }
    }

    companion object {
        private const val TAG = "AtpDownload"
    }
}
