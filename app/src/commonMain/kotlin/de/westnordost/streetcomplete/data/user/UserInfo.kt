package de.westnordost.streetcomplete.data.user

data class UserInfo(
    val id: Long,
    val displayName: String,
    val profileImageUrl: String?,
    val unreadMessagesCount: Int? = null,
)
