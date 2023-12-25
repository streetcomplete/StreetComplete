package de.westnordost.streetcomplete.quests.guidepost

sealed interface GuidepostRefAnswer

data class GuidepostRef(val ref: String) : GuidepostRefAnswer
object NoVisibleGuidepostRef : GuidepostRefAnswer
