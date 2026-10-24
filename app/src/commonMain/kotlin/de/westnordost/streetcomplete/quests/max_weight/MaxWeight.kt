package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.*
import de.westnordost.streetcomplete.util.ktx.toShortString
import kotlinx.serialization.Serializable

@Serializable
sealed interface MaxWeightSign

@Serializable
data class MaxWeight(val type: MaxWeightType, val weight: Weight?) : MaxWeightSign

@Serializable
enum class MaxWeightCategorySweden(val index: Int, val maxWeight: Double) : MaxWeightSign {
    One(1, 64.0),
    Two(2, 51.4),
    Three(3, 37.5),
    Four(4, 74.0)
}

fun MaxWeight.applyTo(tags: Tags) {
    if (weight != null) {
        if (type == MAX_WEIGHT_CATEGORY_SE) {
            val category = MaxWeightCategorySweden.entries.firstOrNull { it.index.toDouble() == weight.value }
            if (category != null) {
                tags["maxweight"] = category.maxWeight.toShortString()

                // See discussion here: https://github.com/streetcomplete/StreetComplete/issues/6586
                // We add the specific traffic_sign codes save the context of this information
                // Also: do not overwrite any existing signs
                if (tags["traffic_sign"] == null) {
                    tags["traffic_sign"] = "SE-CSS-" + category.index.toString()
                }
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
