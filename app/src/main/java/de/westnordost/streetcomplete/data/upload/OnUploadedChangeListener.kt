package de.westnordost.streetcomplete.data.upload

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface OnUploadedChangeListener {
    fun onUploaded(questType: String, at: LatLon)
    fun onDiscarded(questType: String, at: LatLon)
}
