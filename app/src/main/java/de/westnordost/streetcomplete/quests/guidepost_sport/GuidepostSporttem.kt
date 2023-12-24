package de.westnordost.streetcomplete.quests.guidepost_sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.guidepost_sport.GuidepostSport.*
import de.westnordost.streetcomplete.view.image_select.Item

fun GuidepostSport.asItem(): Item<List<GuidepostSport>> =
    Item(listOf(this), iconResId, titleResId)

private val GuidepostSport.iconResId: Int get() = when (this) {
    HIKING ->          R.drawable.ic_guidepost_hiking
    BICYCLE ->         R.drawable.ic_guidepost_cycling
    MTB ->             R.drawable.ic_guidepost_mtb
    CLIMBING ->        R.drawable.ic_guidepost_climbing
    HORSE ->           R.drawable.ic_guidepost_horse_riding
    NORDIC_WALKING ->  R.drawable.ic_guidepost_nordic_walking
    SKI ->             R.drawable.ic_guidepost_ski
    INLINE_SKATING ->  R.drawable.ic_guidepost_inline_skating
    RUNNING ->         R.drawable.ic_guidepost_running
    WINTER_HIKING ->   R.drawable.ic_guidepost_snow_shoe_hiking
}

private val GuidepostSport.titleResId: Int get() = when (this) {
    HIKING ->          R.string.quest_guidepost_sports_hiking
    BICYCLE ->         R.string.quest_guidepost_sports_bicycle
    MTB ->             R.string.quest_guidepost_sports_mtb
    CLIMBING ->        R.string.quest_guidepost_sports_climbing
    HORSE ->           R.string.quest_guidepost_sports_horse
    NORDIC_WALKING ->  R.string.quest_guidepost_sports_nordic_walking
    SKI ->             R.string.quest_guidepost_sports_ski
    INLINE_SKATING ->  R.string.quest_guidepost_sports_inline_skating
    RUNNING ->         R.string.quest_guidepost_sports_running
    WINTER_HIKING ->   R.string.quest_guidepost_sports_winter_hiking
}
