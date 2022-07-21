package de.westnordost.streetcomplete.data.messages

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.ApplicationConstants.QUEST_COUNT_AT_WHICH_TO_SHOW_QUEST_SELECTION_HINT
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.messages.QuestSelectionHintState.NOT_SHOWN
import de.westnordost.streetcomplete.data.messages.QuestSelectionHintState.SHOULD_SHOW
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import java.util.concurrent.CopyOnWriteArrayList

class QuestSelectionHintController(
    private val visibleQuestsSource: VisibleQuestsSource,
    private val prefs: SharedPreferences
) {

    interface Listener {
        fun onQuestSelectionHintStateChanged()
    }
    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    var state: QuestSelectionHintState
        set(value) {
            prefs.edit { putString(Prefs.QUEST_SELECTION_HINT_STATE, value.toString()) }
            listeners.forEach { it.onQuestSelectionHintStateChanged() }
        }
        get() {
            val str = prefs.getString(Prefs.QUEST_SELECTION_HINT_STATE, null)
            return if (str == null) NOT_SHOWN else QuestSelectionHintState.valueOf(str)
        }

    init {
        visibleQuestsSource.addListener(object : VisibleQuestsSource.Listener {
            override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
                if (state == NOT_SHOWN && added.size >= QUEST_COUNT_AT_WHICH_TO_SHOW_QUEST_SELECTION_HINT) {
                    state = SHOULD_SHOW
                }
            }

            override fun onVisibleQuestsInvalidated() {}
        })
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}

enum class QuestSelectionHintState {
    NOT_SHOWN, SHOULD_SHOW, SHOWN
}
