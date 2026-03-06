package de.westnordost.streetcomplete.quests.charging_station_socket

import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class SocketType(val osmKey: String) {
    TYPE2("type2"),
    TYPE2_CABLE("type2_cable"),
    TYPE2_COMBO("type2_combo"),
    CHADEMO("chademo"),
    DOMESTIC("domestic");

    companion object {
        val selectableValues = entries
    }
}

/* ----------------------------------------------------------
   Primary socket icon
   ---------------------------------------------------------- */

val SocketType.icon: DrawableResource
    get() = when (this) {
        SocketType.TYPE2 ->        Res.drawable.socket_type2
        SocketType.TYPE2_CABLE ->  Res.drawable.socket_type2_cable
        SocketType.TYPE2_COMBO ->  Res.drawable.socket_type2_combo
        SocketType.CHADEMO ->      Res.drawable.socket_chademo
        SocketType.DOMESTIC ->     Res.drawable.socket_domestic
    }

/* ----------------------------------------------------------
   EU compatibility label (hexagon symbol)
   Each socket gets its own EU-label icon.
   ---------------------------------------------------------- */

val SocketType.euLabel: DrawableResource
    get() = when (this) {
        SocketType.TYPE2 ->        Res.drawable.socket_eu_type2
        SocketType.TYPE2_CABLE ->  Res.drawable.socket_eu_type2
        SocketType.TYPE2_COMBO ->  Res.drawable.socket_eu_type2_combo
        SocketType.CHADEMO ->      Res.drawable.socket_eu_chademo
        SocketType.DOMESTIC ->     Res.drawable.socket_eu_domestic
    }

/* ----------------------------------------------------------
   Localized title
   ---------------------------------------------------------- */

val SocketType.title: StringResource
    get() = when (this) {
        SocketType.TYPE2 ->        Res.string.quest_charging_station_socket_type2
        SocketType.TYPE2_CABLE ->  Res.string.quest_charging_station_socket_type2_cable
        SocketType.TYPE2_COMBO ->  Res.string.quest_charging_station_socket_type2_combo
        SocketType.CHADEMO ->      Res.string.quest_charging_station_socket_chademo
        SocketType.DOMESTIC ->     Res.string.quest_charging_station_socket_domestic
    }
