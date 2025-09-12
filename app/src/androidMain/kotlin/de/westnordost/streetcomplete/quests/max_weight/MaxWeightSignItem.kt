package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.R

fun MaxWeightSign.getLayoutResourceId(countryCode: String) = when (this) {
    MaxWeightSign.MAX_WEIGHT             -> getMaxWeightSignLayoutResId(countryCode)
    MaxWeightSign.MAX_GROSS_VEHICLE_MASS -> getMaxWeightMgvSignLayoutResId(countryCode)
    MaxWeightSign.MAX_AXLE_LOAD          -> getMaxWeightAxleLoadSignLayoutResId(countryCode)
    MaxWeightSign.MAX_TANDEM_AXLE_LOAD   -> getMaxWeightTandemAxleLoadSignLayoutResId(countryCode)
}

private fun getMaxWeightSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_sign_us
    "FI", "IS", "SE" -> R.layout.quest_maxweight_sign_fi
    else ->             R.layout.quest_maxweight_sign
}

private fun getMaxWeightMgvSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_mgv_sign_us
    "FI", "IS", "SE" -> R.layout.quest_maxweight_mgv_sign_fi
    "DE" ->             R.layout.quest_maxweight_mgv_sign_de
    "GB" ->             R.layout.quest_maxweight_mgv_sign_gb
    else ->             R.layout.quest_maxweight_mgv_sign
}

private fun getMaxWeightAxleLoadSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_axleload_sign_us
    "FI", "IS", "SE" -> R.layout.quest_maxweight_axleload_sign_fi
    else ->             R.layout.quest_maxweight_axleload_sign
}

private fun getMaxWeightTandemAxleLoadSignLayoutResId(countryCode: String): Int = when (countryCode) {
    "AU", "CA", "US" -> R.layout.quest_maxweight_tandem_axleload_sign_us
    "FI", "IS", "SE" -> R.layout.quest_maxweight_tandem_axleload_sign_fi
    else ->             R.layout.quest_maxweight_tandem_axleload_sign
}
