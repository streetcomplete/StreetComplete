package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.sync.IosSyncJob

class IosDownloadController(private val downloader: Downloader) : DownloadController {
    private val job = IosSyncJob(Downloader.TAG)

    override fun download(bbox: BoundingBox, isUserInitiated: Boolean) {
        job.launch(replace = isUserInitiated) { downloader.download(bbox, isUserInitiated) }
    }

    fun close() { job.close() }
}
