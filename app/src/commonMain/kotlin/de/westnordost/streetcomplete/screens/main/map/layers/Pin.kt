package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import org.jetbrains.compose.resources.DrawableResource

/** A quest or edit-history pin, prepared for the shared map's image registry and source. */
data class Pin(
    val position: LatLon,
    val icon: DrawableResource,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val order: Int = 0,
)
