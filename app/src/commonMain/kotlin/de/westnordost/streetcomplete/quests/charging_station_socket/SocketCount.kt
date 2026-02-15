package de.westnordost.streetcomplete.quests.charging_station_socket

import de.westnordost.streetcomplete.osm.Tags
import kotlinx.serialization.Serializable

@Serializable
data class SocketCount(
    val type: SocketType,
    val count: Int
)

fun SocketCount.applyTo(tags: Tags) {
    tags["socket:${type.osmKey}"] = count.toString()
}
