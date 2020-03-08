package de.westnordost.streetcomplete.data.upload

import de.westnordost.osmapi.map.data.LatLon

interface OnUploadedChangeListener {
    fun onUploaded()
    fun onDiscarded(at: LatLon)
}
