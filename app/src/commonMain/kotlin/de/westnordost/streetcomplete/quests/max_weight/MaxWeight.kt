package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.*
import kotlinx.serialization.Serializable

@Serializable
data class MaxWeight(val type: MaxWeightType, val weight: Weight?)

fun MaxWeight.applyTo(tags: Tags) {
    if (weight != null) {
        tags[type.osmKey] = if (type == MAX_WEIGHT_CATEGORY_SE) {
            when (weight.value) {
                1.0 -> "64"
                2.0 -> "51.4"
                3.0 -> "37.5"
                4.0 -> "74"
            }
        } else {
            weight.toOsmString()
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
