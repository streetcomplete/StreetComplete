package de.westnordost.streetcomplete.screens.main.controls

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.platform.InternetConnectionState
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class MainMenuButtonViewModel : ViewModel() {
    abstract val isTeamMode: StateFlow<Boolean>
    abstract var teamModeChanged: Boolean
    abstract val indexInTeam: Int
    abstract fun enableTeamMode(teamSize: Int, indexInTeam: Int)
    abstract fun disableTeamMode()

    abstract val isUserInitiatedDownloadInProgress: Boolean
    abstract val isConnected: Boolean
    abstract fun download(bbox: BoundingBox)
}

class MainMenuButtonViewModelImpl(
    private val teamModeQuestFilter: TeamModeQuestFilter,
    private val downloadController: DownloadController,
    private val downloadProgressSource: DownloadProgressSource,
    private val internetConnectionState: InternetConnectionState
) : MainMenuButtonViewModel() {

    override val isTeamMode = MutableStateFlow(teamModeQuestFilter.isEnabled)

    override val indexInTeam: Int
        get() = teamModeQuestFilter.indexInTeam

    override var teamModeChanged: Boolean = false

    override val isConnected: Boolean
        get() = internetConnectionState.isConnected

    override val isUserInitiatedDownloadInProgress: Boolean
        get() = downloadProgressSource.isUserInitiatedDownloadInProgress

    private val teamModeListener = object : TeamModeQuestFilter.TeamModeChangeListener {
        override fun onTeamModeChanged(enabled: Boolean) {
            teamModeChanged = true
            isTeamMode.value = enabled
        }
    }

    init {
        teamModeQuestFilter.addListener(teamModeListener)
    }

    override fun onCleared() {
        teamModeQuestFilter.removeListener(teamModeListener)
    }

    override fun enableTeamMode(teamSize: Int, indexInTeam: Int) {
        launch(IO) { teamModeQuestFilter.enableTeamMode(teamSize, indexInTeam) }
    }

    override fun disableTeamMode() {
        launch(IO) { teamModeQuestFilter.disableTeamMode() }
    }

    override fun download(bbox: BoundingBox) {
        downloadController.download(bbox, true)
    }
}
