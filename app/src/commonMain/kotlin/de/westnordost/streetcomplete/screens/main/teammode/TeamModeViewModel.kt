package de.westnordost.streetcomplete.screens.main.teammode

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilterController
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.jetbrains.compose.resources.DrawableResource

@Stable
abstract class TeamModeViewModel : ViewModel() {
    abstract val allQuestIcons: List<DrawableResource>
    abstract fun enableTeamMode(teamSize: Int, indexInTeam: Int)
}

@Stable
class TeamModeViewModelImpl(
    questTypeRegistry: QuestTypeRegistry,
    private val teamModeQuestFilterController: TeamModeQuestFilterController,
) : TeamModeViewModel() {

    override val allQuestIcons = questTypeRegistry.map { it.icon }

    override fun enableTeamMode(teamSize: Int, indexInTeam: Int) {
        launch(Dispatchers.IO) { teamModeQuestFilterController.enableTeamMode(teamSize, indexInTeam) }
    }
}
