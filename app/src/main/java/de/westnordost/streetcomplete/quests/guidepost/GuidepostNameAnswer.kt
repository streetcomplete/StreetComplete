package de.westnordost.streetcomplete.quests.guidepost

sealed interface GuidepostNameAnswer

data class GuidepostName(val name: String) : GuidepostNameAnswer
object NoVisibleGuidepostName : GuidepostNameAnswer
