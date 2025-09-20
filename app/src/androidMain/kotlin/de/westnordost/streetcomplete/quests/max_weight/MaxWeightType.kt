package de.westnordost.streetcomplete.quests.max_weight

import kotlinx.serialization.Serializable

@Serializable
enum class MaxWeightType {
    MAX_WEIGHT,
    MAX_WEIGHT_RATING,
    MAX_WEIGHT_RATING_HGV,
    MAX_AXLE_LOAD,
    MAX_TANDEM_AXLE_LOAD,
}
