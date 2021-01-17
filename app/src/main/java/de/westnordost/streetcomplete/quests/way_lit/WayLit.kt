package de.westnordost.streetcomplete.quests.way_lit

enum class WayLit(val osmValue: String) {
    NIGHT_AND_DAY("24/7"),
    AUTOMATIC("automatic"),
    YES("yes"),
    NO("no"),
}

fun Boolean.toWayLit(): WayLit = if (this) WayLit.YES else WayLit.NO
