package de.westnordost.streetcomplete.quests.internet_access

import de.westnordost.streetcomplete.quests.internet_access.InternetAccess.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource

enum class InternetAccess(val osmValue: String) {
    WIFI("wlan"),
    TERMINAL("terminal"),
    WIRED("wired"),
}

val InternetAccess.text: StringResource get() = when (this) {
    WIFI -> Res.string.quest_internet_access_wlan
    TERMINAL -> Res.string.quest_internet_access_terminal
    WIRED -> Res.string.quest_internet_access_wired
}
