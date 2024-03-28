package de.westnordost.streetcomplete.data.visiblequests

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.quest.DayNightCycle.DAY_AND_NIGHT
import de.westnordost.streetcomplete.data.quest.DayNightCycle.ONLY_DAY
import de.westnordost.streetcomplete.data.quest.DayNightCycle.ONLY_NIGHT
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.util.isDay

class DayNightQuestFilter internal constructor(
    private val prefs: ObservableSettings
) {
    var isEnabled = false
        private set

    fun reload() {
        isEnabled = Prefs.DayNightBehavior.valueOf(prefs.getString(Prefs.DAY_NIGHT_BEHAVIOR, "IGNORE")) == Prefs.DayNightBehavior.VISIBILITY
    }

    /*
    Might be an idea to add a listener so this is reevaluated occasionally, or something like that.
    However, I think it's reevaluated everytime the displayed quests are updated?
     */
    fun isVisible(quest: Quest): Boolean {
        if (!isEnabled) return true
        return when (quest.type.dayNightCycle) {
            DAY_AND_NIGHT -> true
            ONLY_DAY -> isDay(quest.position)
            ONLY_NIGHT -> !isDay(quest.position)
        }
    }
}
