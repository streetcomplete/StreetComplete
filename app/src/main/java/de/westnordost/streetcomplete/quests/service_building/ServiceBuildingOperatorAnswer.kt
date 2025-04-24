package de.westnordost.streetcomplete.quests.service_building

sealed interface ServiceBuildingOperatorAnswer

data class ServiceBuildingOperator(val name: String) : ServiceBuildingOperatorAnswer
data object DisusedServiceBuilding : ServiceBuildingOperatorAnswer
