package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource

fun MaxWeightRestriction.getIcon(countryCode: String): DrawableResource = when (this) {
    is MaxWeight -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weight_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_weight_yellow
        else ->             Res.drawable.maxweight_sign_weight
    }
    is MaxWeightRating -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weightrating_mutcd
        "DE" ->             Res.drawable.maxweight_sign_weightrating_de
        "GB" ->             Res.drawable.maxweight_sign_weightrating_gb
        // French "normal" maxweight sign refers to maxweightrating
        "FR" ->             Res.drawable.maxweight_sign_weight
        else ->             Res.drawable.maxweight_sign_weightrating_gb
    }
    is MaxWeightRatingHgv -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weightrating_hgv_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_weightrating_hgv_yellow
        "DE" ->             Res.drawable.maxweight_sign_weightrating_hgv_de
        else ->             Res.drawable.maxweight_sign_weightrating_hgv
    }
    is MaxAxleLoad -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_axleload_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_axleload_yellow
        else ->             Res.drawable.maxweight_sign_axleload
    }
    is MaxTandemAxleLoad -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_bogieweight_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_bogieweight_yellow
        else ->             Res.drawable.maxweight_sign_bogieweight
    }
    // The most common max weight signs in Sweden are the Bärighetsklass signs. These exist with classes 1-4.
    // https://bransch.trafikverket.se/for-dig-i-branschen/vag/bk--barighetsklasser-pa-vagar-och-broar/
    // https://github.com/streetcomplete/StreetComplete/issues/6586
    is MaxWeightCategorySweden -> Res.drawable.maxweight_sign_maxweightclass_se
}
