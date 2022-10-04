package de.westnordost.streetcomplete.quests.fire_hydrant_ref

sealed interface FireHydrantRefAnswer

object NoVisibleFireHydrantRef : FireHydrantRefAnswer
data class FireHydrantRef(val ref: String) : FireHydrantRefAnswer
