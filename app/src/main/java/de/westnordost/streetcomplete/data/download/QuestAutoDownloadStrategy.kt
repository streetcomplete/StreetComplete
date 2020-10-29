package de.westnordost.streetcomplete.data.download

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon

interface QuestAutoDownloadStrategy {
    /** returns the bbox that should be downloaded at this position or null if nothing should be
     *  downloaded now */
    fun getDownloadBoundingBox(pos: LatLon): BoundingBox?
}
