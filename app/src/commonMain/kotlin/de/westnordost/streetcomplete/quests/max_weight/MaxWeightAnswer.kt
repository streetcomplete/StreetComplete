package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT_RATING_HGV
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_TANDEM_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.MAX_WEIGHT

sealed interface MaxWeightAnswer {
    data object NoSign : MaxWeightAnswer
}

data class MaxWeight(val sign: MaxWeightType, val weight: Weight) : MaxWeightAnswer

fun MaxWeight.applyTo(tags: Tags) {
    tags[sign.osmKey] = weight.toOsmString()
}

private val MaxWeightType.osmKey get() = when (this) {
    MAX_WEIGHT            -> "maxweight"
    MAX_WEIGHT_RATING     -> "maxweightrating"
    MAX_WEIGHT_RATING_HGV -> "maxweightrating:hgv"
    MAX_AXLE_LOAD         -> "maxaxleload"
    MAX_TANDEM_AXLE_LOAD  -> "maxbogieweight"
}
