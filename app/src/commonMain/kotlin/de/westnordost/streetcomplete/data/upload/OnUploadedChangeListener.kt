package de.westnordost.streetcomplete.data.upload

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface OnUploadedChangeListener {
    fun onUploaded(editType: String, at: LatLon)
    fun onDiscarded(editType: String, at: LatLon)
}
