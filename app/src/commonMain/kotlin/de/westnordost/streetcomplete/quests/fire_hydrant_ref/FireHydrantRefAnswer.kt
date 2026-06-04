package de.westnordost.streetcomplete.quests.fire_hydrant_ref

sealed interface FireHydrantRefAnswer {
    data object NoSign : FireHydrantRefAnswer
}

data class FireHydrantRef(val ref: String) : FireHydrantRefAnswer
