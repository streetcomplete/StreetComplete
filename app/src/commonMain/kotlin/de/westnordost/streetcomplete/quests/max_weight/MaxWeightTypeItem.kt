package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_TANDEM_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING_HGV
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.maxweight_sign_axleload
import de.westnordost.streetcomplete.resources.maxweight_sign_axleload_mutcd
import de.westnordost.streetcomplete.resources.maxweight_sign_axleload_yellow
import de.westnordost.streetcomplete.resources.maxweight_sign_bogieweight
import de.westnordost.streetcomplete.resources.maxweight_sign_bogieweight_mutcd
import de.westnordost.streetcomplete.resources.maxweight_sign_bogieweight_yellow
import de.westnordost.streetcomplete.resources.maxweight_sign_weight
import de.westnordost.streetcomplete.resources.maxweight_sign_weight_mutcd
import de.westnordost.streetcomplete.resources.maxweight_sign_weight_yellow
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_de
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_gb
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_hgv
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_hgv_de
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_hgv_mutcd
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_hgv_yellow
import de.westnordost.streetcomplete.resources.maxweight_sign_weightrating_mutcd
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
        "FI", "IS", "SE"->  Res.drawable.maxweight_sign_weightrating_hgv_yellow
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
}
