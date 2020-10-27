package de.westnordost.streetcomplete.data.download

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon

interface QuestAutoDownloadStrategy {
    /** returns true if quests should be downloaded automatically at this position now  */
    fun mayDownloadHere(pos: LatLon): Boolean

    /** returns the bbox that should be downloaded at this position (if mayDownloadHere returned true)  */
    fun getDownloadBoundingBox(pos: LatLon): BoundingBox
}
