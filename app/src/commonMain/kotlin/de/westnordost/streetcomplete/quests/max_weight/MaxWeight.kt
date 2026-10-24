package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.*
import kotlinx.serialization.Serializable

@Serializable
sealed interface MaxWeightSign

@Serializable
data class MaxWeight(val type: MaxWeightType, val weight: Weight?) : MaxWeightSign

@Serializable
enum class MaxWeightCategorySweden : MaxWeightSign {
    One, Two, Three, Four
}

fun MaxWeight.applyTo(tags: Tags) {
    if (weight != null) {
        if (type == MAX_WEIGHT_CATEGORY_SE) {
            tags[type.osmKey] = when (weight.value) {
                1.0 -> "64"
                2.0 -> "51.4"
                3.0 -> "37.5"
                4.0 -> "74"
                else -> "0"
            }
            // See discussion here: https://github.com/streetcomplete/StreetComplete/issues/6586
            // We add the specific traffic_sign codes save the context of this information
            // Also: do not overwrite any existing signs
            if (tags["traffic_sign"] == null) {
                tags["traffic_sign"] = "SE-CSS-" + weight.value.toInt()
            }
        } else {
            tags[type.osmKey] = weight.toOsmString()
        }
    }
}

private val MaxWeightType.osmKey get() = when (this) {
    MAX_WEIGHT            -> "maxweight"
    MAX_WEIGHT_RATING     -> "maxweightrating"
    MAX_WEIGHT_RATING_HGV -> "maxweightrating:hgv"
    MAX_AXLE_LOAD         -> "maxaxleload"
    MAX_TANDEM_AXLE_LOAD  -> "maxbogieweight"
    MAX_WEIGHT_CATEGORY_SE -> "maxweight"
}
