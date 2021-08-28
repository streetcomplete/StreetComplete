package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface AutoDownloadStrategy {
    /** returns the bbox that should be downloaded at this position or null if nothing should be
     *  downloaded now */
    fun getDownloadBoundingBox(pos: LatLon): BoundingBox?
}
