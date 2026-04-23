package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.*
import kotlinx.serialization.Serializable

@Serializable
data class MaxWeight(val type: MaxWeightType, val weight: Weight?)

fun MaxWeight.applyTo(tags: Tags) {
    if (weight != null) {
        tags[type.osmKey] = weight.toOsmString()
    }
}

private val MaxWeightType.osmKey get() = when (this) {
    MAX_WEIGHT            -> "maxweight"
    MAX_WEIGHT_RATING     -> "maxweightrating"
    MAX_WEIGHT_RATING_HGV -> "maxweightrating:hgv"
    MAX_AXLE_LOAD         -> "maxaxleload"
    MAX_TANDEM_AXLE_LOAD  -> "maxbogieweight"
}
