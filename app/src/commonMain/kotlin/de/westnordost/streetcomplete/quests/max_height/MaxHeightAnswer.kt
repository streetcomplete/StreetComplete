package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.osm.Length

sealed interface MaxHeightAnswer

data class MaxHeight(val value: Length) : MaxHeightAnswer
data object NoMaxHeightSign : MaxHeightAnswer
