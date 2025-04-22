package de.westnordost.streetcomplete.quests.lamp_mount

sealed interface LampMountAnswer

data class LampMount(val mount: String) : LampMountAnswer
data class Support(val mount: String) : LampMountAnswer
