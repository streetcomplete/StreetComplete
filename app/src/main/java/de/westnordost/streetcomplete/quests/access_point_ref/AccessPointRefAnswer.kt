package de.westnordost.streetcomplete.quests.access_point_ref

sealed interface AccessPointRefAnswer

object NoVisibleAccessPointRef : AccessPointRefAnswer
object IsAssemblyPointAnswer : AccessPointRefAnswer
data class AccessPointRef(val ref: String) : AccessPointRefAnswer
