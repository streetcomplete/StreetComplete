package de.westnordost.streetcomplete.screens.settings.debug

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry

@Stable
abstract class ShowQuestFormsViewModel : ViewModel() {
    abstract val quests: List<QuestType>
}

@Stable
class ShowQuestFormsViewModelImpl(
    private val questTypeRegistry: QuestTypeRegistry
) : ShowQuestFormsViewModel() {
    override val quests get() = questTypeRegistry
}
