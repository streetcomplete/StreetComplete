package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.quest.DayNightCycle.*
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.isDay
import org.koin.java.KoinJavaComponent.inject

class DayNightQuestFilter internal constructor(
    private val prefs: SharedPreferences
) {
    private var enabled = false

    fun reload() {
        enabled = Prefs.DayNightBehavior.valueOf(prefs.getString(Prefs.DAY_NIGHT_BEHAVIOR, "IGNORE")!!) == Prefs.DayNightBehavior.VISIBILITY
    }

    /*
    Might be an idea to add a listener so this is reevaluated occasionally, or something like that.
    However, I think it's reevaluated everytime the displayed quests are updated?
     */
    fun isVisible(quest: Quest): Boolean {
        if (!enabled) return true
        return when (quest.type.dayNightCycle) {
            DAY_AND_NIGHT -> true
            ONLY_DAY -> isDay(quest.position)
            ONLY_NIGHT -> !isDay(quest.position)
        }
    }
}
