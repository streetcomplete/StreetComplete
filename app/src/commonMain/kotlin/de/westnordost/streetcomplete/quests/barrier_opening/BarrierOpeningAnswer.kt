package de.westnordost.streetcomplete.quests.barrier_opening
import de.westnordost.streetcomplete.quests.width.WidthAnswer

sealed interface BarrierOpeningAnswer
data class BarrierWidth(val widthAnswer: WidthAnswer) : BarrierOpeningAnswer
data object BarrierNotWheelchairAccessible : BarrierOpeningAnswer
