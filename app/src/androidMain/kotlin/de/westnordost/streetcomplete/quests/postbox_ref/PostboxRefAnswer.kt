package de.westnordost.streetcomplete.quests.postbox_ref

sealed interface PostboxRefAnswer

data class PostboxRef(val ref: String) : PostboxRefAnswer
data object NoVisiblePostboxRef : PostboxRefAnswer
