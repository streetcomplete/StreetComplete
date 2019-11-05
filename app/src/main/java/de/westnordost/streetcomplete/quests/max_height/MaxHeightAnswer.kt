package de.westnordost.streetcomplete.quests.max_height

sealed class MaxHeightAnswer

data class MaxHeight(val value: HeightMeasure) : MaxHeightAnswer()
data class NoMaxHeightSign(val isTallEnough: Boolean) : MaxHeightAnswer()
