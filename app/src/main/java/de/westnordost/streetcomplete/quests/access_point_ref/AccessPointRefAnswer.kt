package de.westnordost.streetcomplete.quests.access_point_ref

sealed interface AccessPointRefAnswer

object NoAccessPointRef : AccessPointRefAnswer
data class AccessPointRef(val ref: String) : AccessPointRefAnswer
