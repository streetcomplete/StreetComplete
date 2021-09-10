package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.R

enum class Smoothness(val osmValue: String, val title: Int) {
    EXCELLENT("excellent", R.string.quest_smoothness_title_excellent),
    GOOD("good", R.string.quest_smoothness_title_good),
    INTERMEDIATE("intermediate", R.string.quest_smoothness_title_intermediate),
    BAD("bad", R.string.quest_smoothness_title_bad),
    VERY_BAD("very_bad", R.string.quest_smoothness_title_very_bad),
    HORRIBLE("horrible", R.string.quest_smoothness_title_horrible),
    VERY_HORRIBLE("very_horrible", R.string.quest_smoothness_title_very_horrible),
    IMPASSABLE("impassable", R.string.quest_smoothness_title_impassable),
}
