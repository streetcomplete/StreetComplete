package de.westnordost.streetcomplete.quests.width

sealed class WidthAnswer
data class SimpleWidthAnswer(val value : String): WidthAnswer()
data class SidewalkWidthAnswer(var leftSidewalkValue : String?, var rightSidewalkValue : String?): WidthAnswer()
