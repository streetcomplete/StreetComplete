package de.westnordost.streetcomplete.data.download

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

/** Controls downloading */
class DownloadController(private val context: Context) {

    /** Download in at least the given bounding box asynchronously. The next-bigger rectangle
     * in a (z16) tiles grid that encloses the given bounding box will be downloaded.
     *
     * @param bbox the minimum area to download
     * @param isUserInitiated whether this shall be a priority download (cancels previous downloads
     *        and puts itself in the front)
     */
    fun download(bbox: BoundingBox, isUserInitiated: Boolean = false) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            Downloader.TAG,
            if (isUserInitiated) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            DownloadWorker.createWorkRequest(bbox, isUserInitiated)
        )
    }
}
