package de.westnordost.streetcomplete.quests.sidewalk

sealed class SidewalkAnswer
data class SidewalkSides(val left:Boolean, val right:Boolean) : SidewalkAnswer()
object SeparatelyMapped: SidewalkAnswer()
