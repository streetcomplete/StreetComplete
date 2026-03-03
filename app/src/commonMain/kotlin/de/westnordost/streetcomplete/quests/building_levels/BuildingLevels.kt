package de.westnordost.streetcomplete.quests.building_levels

import kotlinx.serialization.Serializable

@Serializable
data class BuildingLevels(val levels: Int, val roofLevels: Int?)
