package de.westnordost.streetcomplete.screens.settings.debug

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry

abstract class ShowQuestFormsViewModel : ViewModel() {
    abstract val quests: List<QuestType>
}

class ShowQuestFormsViewModelImpl(
    private val questTypeRegistry: QuestTypeRegistry
) : ShowQuestFormsViewModel() {
    override val quests get() = questTypeRegistry
}
