package de.westnordost.streetcomplete.quests.access_point_ref

sealed interface AccessPointRefAnswer

data object NoVisibleAccessPointRef : AccessPointRefAnswer
data object IsAssemblyPointAnswer : AccessPointRefAnswer
data class AccessPointRef(val ref: String) : AccessPointRefAnswer
