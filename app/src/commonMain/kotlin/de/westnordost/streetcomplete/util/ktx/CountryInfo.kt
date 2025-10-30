package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cycleway_lane_dashed_both_white
import de.westnordost.streetcomplete.resources.cycleway_lane_dashed_both_white_l
import de.westnordost.streetcomplete.resources.cycleway_lane_dashed_white
import de.westnordost.streetcomplete.resources.cycleway_lane_dashed_white_l
import de.westnordost.streetcomplete.resources.cycleway_lane_dashed_yellow
import de.westnordost.streetcomplete.resources.cycleway_lane_dashed_yellow_l
import de.westnordost.streetcomplete.resources.cycleway_lane_dotted_white
import de.westnordost.streetcomplete.resources.cycleway_lane_dotted_white_l
import de.westnordost.streetcomplete.resources.cycleway_lane_white
import de.westnordost.streetcomplete.resources.cycleway_lane_white_dual
import de.westnordost.streetcomplete.resources.cycleway_lane_white_dual_l
import de.westnordost.streetcomplete.resources.cycleway_lane_white_l
import de.westnordost.streetcomplete.resources.cycleway_lane_yellow
import de.westnordost.streetcomplete.resources.cycleway_lane_yellow_dual
import de.westnordost.streetcomplete.resources.cycleway_lane_yellow_dual_l
import de.westnordost.streetcomplete.resources.cycleway_lane_yellow_l
import de.westnordost.streetcomplete.resources.cycleway_pictograms_white
import de.westnordost.streetcomplete.resources.cycleway_pictograms_white_l
import de.westnordost.streetcomplete.resources.cycleway_pictograms_yellow
import de.westnordost.streetcomplete.resources.cycleway_pictograms_yellow_l
import de.westnordost.streetcomplete.resources.cycleway_shared_lane_no_pictograms
import de.westnordost.streetcomplete.resources.cycleway_shared_lane_orchre_background
import de.westnordost.streetcomplete.resources.cycleway_shared_lane_white_dashed
import de.westnordost.streetcomplete.resources.cycleway_shared_lane_white_dashed_l
import de.westnordost.streetcomplete.resources.no_entry_sign_arrow
import de.westnordost.streetcomplete.resources.no_entry_sign_default
import de.westnordost.streetcomplete.resources.no_entry_sign_do_not_enter
import de.westnordost.streetcomplete.resources.no_entry_sign_no_entre
import de.westnordost.streetcomplete.resources.no_entry_sign_no_entry
import de.westnordost.streetcomplete.resources.no_entry_sign_no_entry_on_white
import de.westnordost.streetcomplete.resources.no_entry_sign_yellow
import de.westnordost.streetcomplete.resources.no_parking
import de.westnordost.streetcomplete.resources.no_parking_australia
import de.westnordost.streetcomplete.resources.no_parking_mutcd
import de.westnordost.streetcomplete.resources.no_parking_mutcd_latin_america
import de.westnordost.streetcomplete.resources.no_parking_mutcd_text
import de.westnordost.streetcomplete.resources.no_parking_mutcd_text_spanish
import de.westnordost.streetcomplete.resources.no_parking_sadc
import de.westnordost.streetcomplete.resources.no_parking_taiwan
import de.westnordost.streetcomplete.resources.no_parking_vienna_variant
import de.westnordost.streetcomplete.resources.no_standing_mutcd_text
import de.westnordost.streetcomplete.resources.no_stopping
import de.westnordost.streetcomplete.resources.no_stopping_australia
import de.westnordost.streetcomplete.resources.no_stopping_canada
import de.westnordost.streetcomplete.resources.no_stopping_colombia
import de.westnordost.streetcomplete.resources.no_stopping_israel
import de.westnordost.streetcomplete.resources.no_stopping_mutcd
import de.westnordost.streetcomplete.resources.no_stopping_mutcd_latin_america
import de.westnordost.streetcomplete.resources.no_stopping_mutcd_text
import de.westnordost.streetcomplete.resources.no_stopping_mutcd_text_spanish
import de.westnordost.streetcomplete.resources.no_stopping_sadc
import de.westnordost.streetcomplete.resources.no_waiting_mutcd_text
import de.westnordost.streetcomplete.resources.shoulder_short_white_dashes
import de.westnordost.streetcomplete.resources.shoulder_short_yellow_dashes
import de.westnordost.streetcomplete.resources.shoulder_two_yellow_lines
import de.westnordost.streetcomplete.resources.shoulder_white_dashes
import de.westnordost.streetcomplete.resources.shoulder_white_line
import de.westnordost.streetcomplete.resources.shoulder_yellow_line
import de.westnordost.streetcomplete.resources.street_marking_double_yellow_zig_zag
import de.westnordost.streetcomplete.resources.street_marking_red
import de.westnordost.streetcomplete.resources.street_marking_red_double
import de.westnordost.streetcomplete.resources.street_marking_red_on_curb
import de.westnordost.streetcomplete.resources.street_marking_red_white_dashes_on_curb
import de.westnordost.streetcomplete.resources.street_marking_white_on_curb
import de.westnordost.streetcomplete.resources.street_marking_yellow
import de.westnordost.streetcomplete.resources.street_marking_yellow_dash_x
import de.westnordost.streetcomplete.resources.street_marking_yellow_dashes
import de.westnordost.streetcomplete.resources.street_marking_yellow_dashes_on_curb
import de.westnordost.streetcomplete.resources.street_marking_yellow_double
import de.westnordost.streetcomplete.resources.street_marking_yellow_on_curb
import de.westnordost.streetcomplete.resources.street_marking_yellow_white_dashes_on_curb
import de.westnordost.streetcomplete.resources.street_marking_yellow_zig_zag
import org.jetbrains.compose.resources.DrawableResource

val CountryInfo.noEntrySignDrawable: DrawableResource get() = when (noEntrySignStyle) {
    "default"           -> Res.drawable.no_entry_sign_default
    "yellow"            -> Res.drawable.no_entry_sign_yellow
    "arrow"             -> Res.drawable.no_entry_sign_arrow
    "do not enter"      -> Res.drawable.no_entry_sign_do_not_enter
    "no entry"          -> Res.drawable.no_entry_sign_no_entry
    "no entre"          -> Res.drawable.no_entry_sign_no_entre
    "no entry on white" -> Res.drawable.no_entry_sign_no_entry_on_white
    else                -> Res.drawable.no_entry_sign_default
}

val CountryInfo.noStandingSignDrawable: DrawableResource? get() = when (noStandingSignStyle) {
    "mutcd text standing" -> Res.drawable.no_standing_mutcd_text
    "mutcd text waiting"  -> Res.drawable.no_waiting_mutcd_text
    else                  -> null
}

val CountryInfo.noParkingSignDrawable: DrawableResource get() = when (noParkingSignStyle) {
    "vienna"             -> Res.drawable.no_parking
    "vienna variant"     -> Res.drawable.no_parking_vienna_variant
    "mutcd"              -> Res.drawable.no_parking_mutcd
    "mutcd text"         -> Res.drawable.no_parking_mutcd_text
    "mutcd latin"        -> Res.drawable.no_parking_mutcd_latin_america
    "mutcd text spanish" -> Res.drawable.no_parking_mutcd_text_spanish
    "sadc"               -> Res.drawable.no_parking_sadc
    "australia"          -> Res.drawable.no_parking_australia
    "taiwan"             -> Res.drawable.no_parking_taiwan
    else                 -> Res.drawable.no_parking
}

val CountryInfo.noStoppingSignDrawable: DrawableResource get() = when (noStoppingSignStyle) {
    "vienna"             -> Res.drawable.no_stopping
    "mutcd"              -> Res.drawable.no_stopping_mutcd
    "mutcd latin"        -> Res.drawable.no_stopping_mutcd_latin_america
    "mutcd text"         -> Res.drawable.no_stopping_mutcd_text
    "mutcd text spanish" -> Res.drawable.no_stopping_mutcd_text_spanish
    "sadc"               -> Res.drawable.no_stopping_sadc
    "australia"          -> Res.drawable.no_stopping_australia
    "colombia"           -> Res.drawable.no_stopping_colombia
    "canada"             -> Res.drawable.no_stopping_canada
    "israel"             -> Res.drawable.no_stopping_israel
    else                 -> Res.drawable.no_stopping
}

val CountryInfo.noParkingLineStyleDrawable: DrawableResource? get() =
    noParkingLineStyle.asLineStyleDrawable

val CountryInfo.noStandingLineStyleDrawable: DrawableResource? get() =
    noStandingLineStyle.asLineStyleDrawable

val CountryInfo.noStoppingLineStyleDrawable: DrawableResource? get() =
    noStoppingLineStyle.asLineStyleDrawable

private val String?.asLineStyleDrawable: DrawableResource? get() = when (this) {
    "yellow"                 -> Res.drawable.street_marking_yellow
    "dashed yellow"          -> Res.drawable.street_marking_yellow_dashes
    "double yellow"          -> Res.drawable.street_marking_yellow_double
    "yellow zig-zags"        -> Res.drawable.street_marking_yellow_zig_zag
    "double yellow zig-zags" -> Res.drawable.street_marking_double_yellow_zig_zag
    "dashed yellow with Xs"  -> Res.drawable.street_marking_yellow_dash_x
    "red"                    -> Res.drawable.street_marking_red
    "double red"             -> Res.drawable.street_marking_red_double
    "yellow on curb"         -> Res.drawable.street_marking_yellow_on_curb
    "red on curb"            -> Res.drawable.street_marking_red_on_curb
    "white on curb"          -> Res.drawable.street_marking_white_on_curb
    "dashed yellow on curb"  -> Res.drawable.street_marking_yellow_dashes_on_curb
    "red-white on curb"      -> Res.drawable.street_marking_red_white_dashes_on_curb
    "yellow-white on curb"   -> Res.drawable.street_marking_yellow_white_dashes_on_curb
    else -> null
}

val CountryInfo.shoulderLineStyleDrawable: DrawableResource get() = when (edgeLineStyle) {
    "white" ->               Res.drawable.shoulder_white_line
    "yellow" ->              Res.drawable.shoulder_yellow_line
    "short white dashes" ->  Res.drawable.shoulder_short_white_dashes
    "white dashes" ->        Res.drawable.shoulder_white_dashes
    "short yellow dashes" -> Res.drawable.shoulder_short_yellow_dashes
    "two yellow lines" ->    Res.drawable.shoulder_two_yellow_lines
    else ->                  Res.drawable.shoulder_white_line
}

val CountryInfo.exclusiveCycleLaneDrawable: DrawableResource get() = when (exclusiveCycleLaneStyle) {
    "white" ->                      Res.drawable.cycleway_lane_white
    "yellow" ->                     Res.drawable.cycleway_lane_yellow
    "white dots" ->                 Res.drawable.cycleway_lane_dotted_white
    "white dashes" ->               Res.drawable.cycleway_lane_dashed_white
    "white dashes on both sides" -> Res.drawable.cycleway_lane_dashed_both_white
    "yellow dashes" ->              Res.drawable.cycleway_lane_dashed_yellow
    else ->                         Res.drawable.cycleway_lane_white
}

val CountryInfo.exclusiveCycleLaneMirroredDrawable: DrawableResource get() = when (exclusiveCycleLaneStyle) {
    "white" ->                      Res.drawable.cycleway_lane_white_l
    "yellow" ->                     Res.drawable.cycleway_lane_yellow_l
    "white dots" ->                 Res.drawable.cycleway_lane_dotted_white_l
    "white dashes" ->               Res.drawable.cycleway_lane_dashed_white_l
    "white dashes on both sides" -> Res.drawable.cycleway_lane_dashed_both_white_l
    "yellow dashes" ->              Res.drawable.cycleway_lane_dashed_yellow_l
    else ->                         Res.drawable.cycleway_lane_white_l
}

val CountryInfo.dualCycleLaneDrawable: DrawableResource get() = when {
    exclusiveCycleLaneStyle.startsWith("white") ->  Res.drawable.cycleway_lane_white_dual
    exclusiveCycleLaneStyle.startsWith("yellow") -> Res.drawable.cycleway_lane_yellow_dual
    else ->                                         Res.drawable.cycleway_lane_white_dual
}

val CountryInfo.dualCycleLaneMirroredDrawable: DrawableResource get() = when {
    exclusiveCycleLaneStyle.startsWith("white") ->  Res.drawable.cycleway_lane_white_dual_l
    exclusiveCycleLaneStyle.startsWith("yellow") -> Res.drawable.cycleway_lane_yellow_dual_l
    else ->                                         Res.drawable.cycleway_lane_white_dual_l
}

val CountryInfo.advisoryCycleLaneDrawable: DrawableResource get() = when (advisoryCycleLaneStyle) {
    "white dashes" ->                   Res.drawable.cycleway_shared_lane_white_dashed
    "white dashes without pictogram" -> Res.drawable.cycleway_shared_lane_no_pictograms
    "ochre background" ->               Res.drawable.cycleway_shared_lane_orchre_background
    else ->                             Res.drawable.cycleway_shared_lane_white_dashed
}

val CountryInfo.advisoryCycleLaneMirroredDrawable: DrawableResource get() = when (advisoryCycleLaneStyle) {
    "white dashes" ->                   Res.drawable.cycleway_shared_lane_white_dashed_l
    "white dashes without pictogram" -> Res.drawable.cycleway_shared_lane_no_pictograms
    "ochre background" ->               Res.drawable.cycleway_shared_lane_orchre_background
    else ->                             Res.drawable.cycleway_shared_lane_white_dashed_l
}

val CountryInfo.pictogramCycleLaneDrawable: DrawableResource get() = when (pictogramCycleLaneStyle) {
    "white" ->  Res.drawable.cycleway_pictograms_white
    "yellow" -> Res.drawable.cycleway_pictograms_yellow
    else ->     Res.drawable.cycleway_pictograms_white
}

val CountryInfo.pictogramCycleLaneMirroredDrawable: DrawableResource get() = when (pictogramCycleLaneStyle) {
    "white" ->  Res.drawable.cycleway_pictograms_white_l
    "yellow" -> Res.drawable.cycleway_pictograms_yellow_l
    else ->     Res.drawable.cycleway_pictograms_white_l
}
