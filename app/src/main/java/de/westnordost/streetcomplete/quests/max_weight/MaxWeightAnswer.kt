package de.westnordost.streetcomplete.quests.max_weight

sealed class MaxWeightAnswer

data class MaxWeight(val value: Measure) : MaxWeightAnswer()
data class NoMaxWeightSign(val dummy: String) : MaxWeightAnswer()
