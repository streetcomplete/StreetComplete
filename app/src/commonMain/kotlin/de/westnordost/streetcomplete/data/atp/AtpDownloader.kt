package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.contains
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/** Takes care of downloading ATP data into persistent storage */
class AtpDownloader(
    private val atpApi: AtpApiClient,
    private val atpController: AtpController
) {
    suspend fun download(bbox: BoundingBox) {
        try {
            // ATP data download failing should not take down entire download
            val time = nowAsEpochMilliseconds()
            val entries: Collection<AtpEntry> = atpApi.getAllAtpEntries(bbox)
            val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
            Log.i(TAG, "Downloaded ${entries.size} ATP entries in ${seconds.format(1)}s")
            yield()
            withContext(Dispatchers.IO) { atpController.putAllForBBox(bbox, entries) }
        } catch (e: Exception) {
            Log.w(TAG, e.message.orEmpty(), e)
        }
    }

    companion object {
        private const val TAG = "AtpDownload"
    }
}
