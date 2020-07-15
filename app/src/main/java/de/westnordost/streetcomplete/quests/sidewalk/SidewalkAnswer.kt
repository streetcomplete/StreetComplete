package de.westnordost.streetcomplete.quests.sidewalk

abstract class SidewalkAnswer
open class SidewalkSides(val left:Boolean, val right:Boolean) : SidewalkAnswer()
object SeparatelyMapped: SidewalkAnswer()
