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
    "vienna"    -> R.drawable.ic_living_street
    "sadc"      -> R.drawable.ic_living_street_sadc
    "russia"    -> R.drawable.ic_living_street_russia
    "france"    -> R.drawable.ic_living_street_france
    "australia" -> R.drawable.ic_living_street_australia
    "mexico"    -> R.drawable.ic_living_street_mexico
    else        -> null
}

val CountryInfo.noEntrySignDrawableResId: Int get() = when (noEntrySignStyle) {
    "default"           -> R.drawable.ic_no_entry_sign_default
    "yellow"            -> R.drawable.ic_no_entry_sign_yellow
    "arrow"             -> R.drawable.ic_no_entry_sign_arrow
    "do not enter"      -> R.drawable.ic_no_entry_sign_do_not_enter
    "no entry"          -> R.drawable.ic_no_entry_sign_no_entry
    "no entre"          -> R.drawable.ic_no_entry_sign_no_entre
    "no entry on white" -> R.drawable.ic_no_entry_sign_no_entry_on_white
    else                -> R.drawable.ic_no_entry_sign_default
}

val CountryInfo.noStandingSignDrawableResId: Int? get() = when (noStandingSignStyle) {
    "mutcd text standing" -> R.drawable.ic_no_standing_mutcd_text
    "mutcd text waiting"  -> R.drawable.ic_no_waiting_mutcd_text
    else                  -> null
}

val CountryInfo.noParkingSignDrawableResId: Int get() = when (noParkingSignStyle) {
    "vienna"             -> R.drawable.ic_no_parking
    "vienna variant"     -> R.drawable.ic_no_parking_vienna_variant
    "mutcd"              -> R.drawable.ic_no_parking_mutcd
    "mutcd text"         -> R.drawable.ic_no_parking_mutcd_text
    "mutcd latin"        -> R.drawable.ic_no_parking_mutcd_latin_america
    "mutcd text spanish" -> R.drawable.ic_no_parking_mutcd_text_spanish
    "sadc"               -> R.drawable.ic_no_parking_sadc
    "australia"          -> R.drawable.ic_no_parking_australia
    "taiwan"             -> R.drawable.ic_no_parking_taiwan
    else                 -> R.drawable.ic_no_parking
}

val CountryInfo.noStoppingSignDrawableResId: Int get() = when (noStoppingSignStyle) {
    "vienna"             -> R.drawable.ic_no_stopping
    "mutcd"              -> R.drawable.ic_no_stopping_mutcd
    "mutcd latin"        -> R.drawable.ic_no_stopping_mutcd_latin_america
    "mutcd text"         -> R.drawable.ic_no_stopping_mutcd_text
    "mutcd text spanish" -> R.drawable.ic_no_stopping_mutcd_text_spanish
    "sadc"               -> R.drawable.ic_no_stopping_sadc
    "australia"          -> R.drawable.ic_no_stopping_australia
    "colombia"           -> R.drawable.ic_no_stopping_colombia
    "canada"             -> R.drawable.ic_no_stopping_canada
    "israel"             -> R.drawable.ic_no_stopping_israel
    else                 -> R.drawable.ic_no_stopping
}

val CountryInfo.noParkingLineStyleResId: Int? get() = noParkingLineStyle.asLineStyleResId

val CountryInfo.noStandingLineStyleResId: Int? get() = noStandingLineStyle.asLineStyleResId

val CountryInfo.noStoppingLineStyleResId: Int? get() = noStoppingLineStyle.asLineStyleResId

private val String?.asLineStyleResId: Int? get() = when (this) {
    "yellow"                 -> R.drawable.ic_street_marking_yellow
    "dashed yellow"          -> R.drawable.ic_street_marking_yellow_dashes
    "double yellow"          -> R.drawable.ic_street_marking_yellow_double
    "yellow zig-zags"        -> R.drawable.ic_street_marking_yellow_zig_zag
    "double yellow zig-zags" -> R.drawable.ic_street_marking_double_yellow_zig_zag
    "dashed yellow with Xs"  -> R.drawable.ic_street_marking_yellow_dash_x
    "red"                    -> R.drawable.ic_street_marking_red
    "double red"             -> R.drawable.ic_street_marking_red_double
    "yellow on curb"         -> R.drawable.ic_street_marking_yellow_on_curb
    "red on curb"            -> R.drawable.ic_street_marking_red_on_curb
    "white on curb"          -> R.drawable.ic_street_marking_white_on_curb
    "dashed yellow on curb"  -> R.drawable.ic_street_marking_yellow_dashes_on_curb
    "red-white on curb"      -> R.drawable.ic_street_marking_red_white_dashes_on_curb
    "yellow-white on curb"   -> R.drawable.ic_street_marking_yellow_white_dashes_on_curb
    else -> null
}

val CountryInfo.shoulderLineStyleResId: Int get() = when (edgeLineStyle) {
    "white" -> R.drawable.ic_shoulder_white_line
    "yellow" -> R.drawable.ic_shoulder_yellow_line
    "short white dashes" -> R.drawable.ic_shoulder_short_white_dashes
    "white dashes" -> R.drawable.ic_shoulder_white_dashes
    "short yellow dashes" -> R.drawable.ic_shoulder_short_yellow_dashes
    "two yellow lines" -> R.drawable.ic_shoulder_two_yellow_lines
    else -> R.drawable.ic_shoulder_white_line
}
