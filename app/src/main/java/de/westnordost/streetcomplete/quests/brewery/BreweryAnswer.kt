package de.westnordost.streetcomplete.quests.brewery

sealed interface BreweryAnswer

data class BreweryStringAnswer(val brewery: String) : BreweryAnswer
object ManyBeersAnswer : BreweryAnswer
object NoBeerAnswer : BreweryAnswer
