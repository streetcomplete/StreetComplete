package de.westnordost.streetcomplete.quests.ferry

sealed interface FerryBicycleAccessAnswer
object BicycleAllowed : FerryBicycleAccessAnswer
object BicycleNotAllowed : FerryBicycleAccessAnswer
object BicycleNotSigned : FerryBicycleAccessAnswer
