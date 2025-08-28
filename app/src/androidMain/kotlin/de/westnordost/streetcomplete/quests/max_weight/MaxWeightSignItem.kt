package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.sign_maxaxleload
import de.westnordost.streetcomplete.resources.sign_maxaxleload_mutcd
import de.westnordost.streetcomplete.resources.sign_maxaxleload_yellow
import de.westnordost.streetcomplete.resources.sign_maxbogieweight
import de.westnordost.streetcomplete.resources.sign_maxbogieweight_mutcd
import de.westnordost.streetcomplete.resources.sign_maxbogieweight_yellow
import de.westnordost.streetcomplete.resources.sign_maxweight
import de.westnordost.streetcomplete.resources.sign_maxweight_mutcd
import de.westnordost.streetcomplete.resources.sign_maxweight_yellow
import de.westnordost.streetcomplete.resources.sign_maxweightrating_gb
import de.westnordost.streetcomplete.resources.sign_maxweightrating_hgv
import de.westnordost.streetcomplete.resources.sign_maxweightrating_hgv_de
import de.westnordost.streetcomplete.resources.sign_maxweightrating_hgv_yellow
import de.westnordost.streetcomplete.resources.sign_maxweightrating_mutcd
import org.jetbrains.compose.resources.DrawableResource

fun MaxWeightType.getIcon(countryCode: String): DrawableResource? = when (this) {
    MaxWeightType.MAX_WEIGHT -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.sign_maxweight_mutcd
        "FI", "IS", "SE" -> Res.drawable.sign_maxweight_yellow
        else ->             Res.drawable.sign_maxweight
    }
    MaxWeightType.MAX_WEIGHT_RATING -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.sign_maxweightrating_mutcd
        "GB" ->             Res.drawable.sign_maxweightrating_gb
        else ->             null
    }
    MaxWeightType.MAX_WEIGHT_RATING_HGV -> when (countryCode) {
        "AU", "CA", "US" -> null
        "FI", "IS", "SE"->  Res.drawable.sign_maxweightrating_hgv_yellow
        "DE" ->             Res.drawable.sign_maxweightrating_hgv_de
        "GB" ->             null
        else ->             Res.drawable.sign_maxweightrating_hgv
    }
    MaxWeightType.MAX_AXLE_LOAD -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.sign_maxaxleload_mutcd
        "FI", "IS", "SE" -> Res.drawable.sign_maxaxleload_yellow
        else ->             Res.drawable.sign_maxaxleload
    }
    MaxWeightType.MAX_TANDEM_AXLE_LOAD -> when (countryCode) {
        "AU", "CA", "US" -> Res.drawable.sign_maxbogieweight_mutcd
        "FI", "IS", "SE" -> Res.drawable.sign_maxbogieweight_yellow
        else ->             Res.drawable.sign_maxbogieweight
    }
}
