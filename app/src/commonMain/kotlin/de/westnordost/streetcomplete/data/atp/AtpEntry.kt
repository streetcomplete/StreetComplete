package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.serialization.Serializable

@Serializable
data class AtpEntry(
    // should we have separate Dao, Downloader, Controller etc for cases
    // where it represents a new OSM Element and cases where it represents
    // mismatching opening hours data?
    val position: LatLon,
    val id: Long,
    val osmMatch: ElementKey?,
    val tagsInATP: Map<String, String>,
    val tagsInOSM: Map<String, String>?,
)
