package de.westnordost.streetcomplete.quests.max_weight

sealed class MaxWeightAnswer

data class MaxWeight(val value: WeightMeasure) : MaxWeightAnswer()
object NoMaxWeightSign : MaxWeightAnswer()
