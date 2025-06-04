package de.westnordost.streetcomplete.data.upload

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface UploadProgressSource {
    interface Listener {
        fun onStarted() {}
        fun onUploaded(editType: String, at: LatLon) {}
        fun onDiscarded(editType: String, at: LatLon) {}
        fun onError(e: Exception) {}
        fun onFinished() {}
    }

    val isUploadInProgress: Boolean

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
