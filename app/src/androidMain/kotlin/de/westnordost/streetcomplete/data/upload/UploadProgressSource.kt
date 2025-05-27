package de.westnordost.streetcomplete.data.upload

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface UploadProgressSource {
    interface Listener {
        fun onStarted() {}
        fun onUploaded(questType: String, at: LatLon) {}
        fun onDiscarded(questType: String, at: LatLon) {}
        fun onError(e: Exception) {}
        fun onFinished() {}
    }

    val isUploadInProgress: Boolean

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
