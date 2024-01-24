package de.westnordost.streetcomplete.quests.max_weight

sealed interface MaxWeightAnswer

data class MaxWeight(val sign: MaxWeightSign, val weight: Weight) : MaxWeightAnswer
data object NoMaxWeightSign : MaxWeightAnswer
