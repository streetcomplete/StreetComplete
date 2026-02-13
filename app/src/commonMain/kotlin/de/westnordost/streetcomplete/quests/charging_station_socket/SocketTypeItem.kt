package de.westnordost.streetcomplete.quests.charging_station_socket

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val SocketType.icon: DrawableResource get() = when (this) {
    SocketType.TYPE2 ->        Res.drawable.socket_type2
    SocketType.TYPE2_CABLE ->  Res.drawable.socket_type2_cable
    SocketType.TYPE2_COMBO ->  Res.drawable.socket_type2_combo
    SocketType.CHADEMO ->      Res.drawable.socket_chademo
    SocketType.DOMESTIC ->     Res.drawable.socket_domestic
}

val SocketType.title: StringResource get() = when (this) {
    SocketType.TYPE2 ->        Res.string.quest_charging_station_socket_type2
    SocketType.TYPE2_CABLE ->  Res.string.quest_charging_station_socket_type2_cable
    SocketType.TYPE2_COMBO ->  Res.string.quest_charging_station_socket_type2_combo
    SocketType.CHADEMO ->      Res.string.quest_charging_station_socket_chademo
    SocketType.DOMESTIC ->     Res.string.quest_charging_station_socket_domestic
}
