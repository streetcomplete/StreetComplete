package de.westnordost.streetcomplete.quests.max_weight

sealed class MaxWeightAnswer

data class MaxWeight(val sign: MaxWeightSign, val weight: Weight) : MaxWeightAnswer()
object NoMaxWeightSign : MaxWeightAnswer()
