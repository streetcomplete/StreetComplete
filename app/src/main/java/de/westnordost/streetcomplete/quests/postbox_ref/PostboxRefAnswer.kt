package de.westnordost.streetcomplete.quests.postbox_ref

sealed interface PostboxRefAnswer

data class Ref(val ref: String) : PostboxRefAnswer
object NoRefVisible : PostboxRefAnswer
