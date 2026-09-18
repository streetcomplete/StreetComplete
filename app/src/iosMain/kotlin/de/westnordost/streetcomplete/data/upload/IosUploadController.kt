package de.westnordost.streetcomplete.data.upload

import de.westnordost.streetcomplete.data.sync.IosSyncJob

class IosUploadController(private val uploader: Uploader) : UploadController {
    private val job = IosSyncJob(Uploader.TAG)

    override fun upload(isUserInitiated: Boolean) {
        job.launch { uploader.upload() }
    }

    fun close() { job.close() }
}
