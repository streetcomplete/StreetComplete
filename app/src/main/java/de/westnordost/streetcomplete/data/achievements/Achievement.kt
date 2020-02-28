package de.westnordost.streetcomplete.data.achievements

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Achievement(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val description: Int?,
    val condition: AchievementCondition,
    val levels: List<AchievementLevel>
)

data class AchievementLevel(val threshold: Int, val links: List<Link>)

sealed class AchievementCondition

data class SolvedQuestsOfTypes(val questTypes: List<String>) : AchievementCondition()
object TotalSolvedQuests : AchievementCondition()
object DaysActive : AchievementCondition()
