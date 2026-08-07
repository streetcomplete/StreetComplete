package de.westnordost.streetcomplete.quests.access_point_ref

sealed interface AccessPointRefAnswer {
    data object IsAssemblyPoint : AccessPointRefAnswer
    data object NoRef : AccessPointRefAnswer
}

data class AccessPointRef(val ref: String) : AccessPointRefAnswer
