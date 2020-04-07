package de.westnordost.streetcomplete.data.upload

interface UploadProgressSource {
    fun addUploadProgressListener(listener: UploadProgressListener)
    fun removeUploadProgressListener(listener: UploadProgressListener)
}