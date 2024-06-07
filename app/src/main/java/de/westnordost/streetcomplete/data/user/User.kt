package de.westnordost.streetcomplete.data.user

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Long, val name: String)
