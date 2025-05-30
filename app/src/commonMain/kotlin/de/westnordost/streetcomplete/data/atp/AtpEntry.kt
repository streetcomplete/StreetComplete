package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.serialization.Serializable

@Serializable
data class AtpEntry(
    // should we have separate Dao, Downloader, Controller etc for cases
    // where it represents a new OSM Element and cases where it represents
    // mismatching opening hours data?
    val position: LatLon,
    val id: Long,
    val osmElementMatchId: Long?,
    val osmElementMatchType: String?, // surely there is a better way to do this rather than keeping object id and object type in separate variables
    // TODO - how elements are being stored elsewhere in app?
    // TODO maybe use sealed class Element
    // but do we have version data? maybe we should have version data available, just in case?
    // may require changing ATP API
    // maybe just store fake data for now?
    val tagsInATP: Map<String, String>,
    val tagsInOSM: Map<String, String>?,
)
