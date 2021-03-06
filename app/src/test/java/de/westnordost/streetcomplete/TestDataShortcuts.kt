package de.westnordost.streetcomplete

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import java.util.*

fun p(lat: Double = 0.0, lon: Double = 0.0) = OsmLatLon(lat, lon)

fun node(
    id: Long = 1L,
    version: Int = 1,
    pos: LatLon = p(),
    tags: Map<String,String>? = null,
    date: Date? = null
) = OsmNode(id, version, pos, tags, null, date)
