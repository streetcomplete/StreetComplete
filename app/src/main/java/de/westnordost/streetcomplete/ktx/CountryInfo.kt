package de.westnordost.streetcomplete.ktx

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo

val CountryInfo.advisorySpeedLimitSignLayoutResId: Int? get() = when(advisorySpeedLimitSignStyle) {
    "blue"  -> R.layout.quest_maxspeed_advisory_blue
    "yellow"  -> R.layout.quest_maxspeed_advisory_yellow
    "white"  -> R.layout.quest_maxspeed_advisory_white
    else    -> null
}

val CountryInfo.livingStreetSignDrawableResId: Int? get() = when(livingStreetSignStyle) {
    "wide"      -> R.drawable.ic_living_street
    "portrait"  -> R.drawable.ic_living_street_portrait
    "square"    -> R.drawable.ic_living_street_square
    "sadc"      -> R.drawable.ic_living_street_sadc
    "australia" -> R.drawable.ic_living_street_australia
    "mexico"    -> R.drawable.ic_living_street_mexico
    else        -> null
}

val CountryInfo.noStandingSignDrawableResId: Int? get() = when(noStandingSignStyle) {
    "mutcd text standing" -> R.drawable.ic_no_standing_mutcd_text
    "mutcd text waiting"  -> R.drawable.ic_no_waiting_mutcd_text
    else                  -> null
}

val CountryInfo.noParkingSignDrawableResId: Int? get() = when(noParkingSignStyle) {
    "vienna"             -> R.drawable.ic_no_parking
    "vienna variant"     -> R.drawable.ic_no_parking_vienna
    "mutcd"              -> R.drawable.ic_no_parking_mutcd
    "mutcd text"         -> R.drawable.ic_no_parking_mutcd_text
    "mutcd latin"        -> R.drawable.ic_no_parking_mutcd_latin_america
    "mutcd text spanish" -> R.drawable.ic_no_parking_mutcd_text_spanish
    "sadc"               -> R.drawable.ic_no_parking_sadc
    "australia"          -> R.drawable.ic_no_parking_australia
    "taiwan"             -> R.drawable.ic_no_parking_taiwan
    else                 -> null
}

val CountryInfo.noStoppingSignDrawableResId: Int? get() = when(noStoppingSignStyle) {
    "vienna"      -> R.drawable.ic_no_stopping
    "mutcd"       -> R.drawable.ic_no_stopping_mutcd
    "mutcd latin" -> R.drawable.ic_no_stopping_mutcd_latin_america
    "mutcd text"  -> R.drawable.ic_no_stopping_mutcd_text
    "sadc"        -> R.drawable.ic_no_stopping_sadc
    "australia"   -> R.drawable.ic_no_stopping_australia
    "colombia"    -> R.drawable.ic_no_stopping_colombia
    "canada"      -> R.drawable.ic_no_stopping_canada
    else          -> null
}
