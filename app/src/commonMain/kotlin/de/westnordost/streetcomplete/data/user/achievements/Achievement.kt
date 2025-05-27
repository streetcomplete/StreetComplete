package de.westnordost.streetcomplete.data.user.achievements


data class Achievement(
    val id: String,
    val icon: Int,
    val title: Int,
    val description: Int?,
    val condition: AchievementCondition,
    val pointsNeededToAdvanceFunction: (Int) -> Int,
    val unlockedLinks: Map<Int, List<Link>>,
    val maxLevel: Int = -1
) {
    fun getPointThreshold(level: Int): Int {
        var threshold = 0
        for (i in 0 until level) {
            threshold += pointsNeededToAdvanceFunction(i)
        }
        return threshold
    }
}

sealed interface AchievementCondition {
    data object EditsOfTypeCount : AchievementCondition
    data object TotalEditCount : AchievementCondition
    data object DaysActive : AchievementCondition
}
