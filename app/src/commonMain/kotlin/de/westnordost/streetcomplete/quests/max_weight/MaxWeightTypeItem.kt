package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_TANDEM_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_CATEGORY_SE
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING_HGV
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource

fun MaxWeightType.getIcon(countryCode: String): DrawableResource? = when (this) {
    MAX_WEIGHT -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weight_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_weight_yellow
        // no weight sign in France (see maxweightrating signs) - #6686
        "FR" ->             null
        else ->             Res.drawable.maxweight_sign_weight
    }
    MAX_WEIGHT_RATING -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weightrating_mutcd
        "DE" ->             Res.drawable.maxweight_sign_weightrating_de
        "GB" ->             Res.drawable.maxweight_sign_weightrating_gb
        // French "normal" maxweight sign refers to maxweightrating
        "FR" ->             Res.drawable.maxweight_sign_weight
        else ->             null
    }
    MAX_WEIGHT_RATING_HGV -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_weightrating_hgv_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_weightrating_hgv_yellow
        "DE" ->             Res.drawable.maxweight_sign_weightrating_hgv_de
        "GB" ->             null // no max weight rating HGV sign in GB
        else ->             Res.drawable.maxweight_sign_weightrating_hgv
    }
    MAX_AXLE_LOAD -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_axleload_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_axleload_yellow
        else ->             Res.drawable.maxweight_sign_axleload
    }
    MAX_TANDEM_AXLE_LOAD -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.maxweight_sign_bogieweight_mutcd
        "FI", "IS", "SE" -> Res.drawable.maxweight_sign_bogieweight_yellow
        else ->             Res.drawable.maxweight_sign_bogieweight
    }
    // The most common max weight in sweden are the Bärighetsklass signs. These exist with classes 1-4.
    // https://bransch.trafikverket.se/for-dig-i-branschen/vag/bk--barighetsklasser-pa-vagar-och-broar/
    // https://github.com/streetcomplete/StreetComplete/issues/6586
    MAX_WEIGHT_CATEGORY_SE -> when (countryCode) {
        "SE" ->             Res.drawable.sign_maxweightclass_se
        else ->             null
    }
}
