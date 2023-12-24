package de.westnordost.streetcomplete.quests.guidepost_sport


sealed interface GuidepostSportsAnswer

object IsSimpleGuidepost : GuidepostSportsAnswer
data class SelectedGuidepostSports(val selectedSports: List<GuidepostSport>) : GuidepostSportsAnswer
