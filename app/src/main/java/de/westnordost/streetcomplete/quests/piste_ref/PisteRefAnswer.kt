package de.westnordost.streetcomplete.quests.piste_ref

sealed interface PisteRefAnswer

object PisteConnection : PisteRefAnswer

data class PisteRef(val ref: String) : PisteRefAnswer
