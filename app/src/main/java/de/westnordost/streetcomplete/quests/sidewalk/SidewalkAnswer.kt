package de.westnordost.streetcomplete.quests.sidewalk

sealed class SidewalkAnswer
open class SidewalkSides(val left:Boolean, val right:Boolean) : SidewalkAnswer()
object SeparatelyMapped: SidewalkAnswer()
