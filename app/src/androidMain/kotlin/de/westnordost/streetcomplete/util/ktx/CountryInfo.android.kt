package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo

val CountryInfo.advisorySpeedLimitSignLayoutResId: Int? get() = when (advisorySpeedLimitSignStyle) {
    "blue" -> R.layout.quest_maxspeed_advisory_blue
    "yellow" -> R.layout.quest_maxspeed_advisory_yellow
    "white" -> R.layout.quest_maxspeed_advisory_white
    else -> null
}

val CountryInfo.livingStreetSignDrawableResId: Int? get() = when (livingStreetSignStyle) {
    "vienna"    -> R.drawable.living_street
    "sadc"      -> R.drawable.living_street_sadc
    "russia"    -> R.drawable.living_street_russia
    "france"    -> R.drawable.living_street_france
    "australia" -> R.drawable.living_street_australia
    "mexico"    -> R.drawable.living_street_mexico
    else        -> null
}
