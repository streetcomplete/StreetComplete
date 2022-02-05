package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.osm.Length

sealed class MaxHeightAnswer

data class MaxHeight(val value: Length) : MaxHeightAnswer()
data class NoMaxHeightSign(val isTallEnough: Boolean) : MaxHeightAnswer()
