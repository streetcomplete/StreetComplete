package de.westnordost.streetcomplete.screens.main

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.overlays.Overlay
import kotlinx.coroutines.flow.StateFlow

abstract class MainViewModel : ViewModel() {
    /* messages */
    abstract val messagesCount: StateFlow<Int>
    abstract suspend fun popMessage(): Message?

    /* overlays */
    abstract val selectedOverlay: StateFlow<Overlay?>
    abstract val overlays: List<Overlay>

    abstract val hasShownOverlaysTutorial: Boolean

    abstract fun selectOverlay(overlay: Overlay?)

    /* team mode */
    abstract val isTeamMode: StateFlow<Boolean>
    abstract var teamModeChanged: Boolean
    abstract val indexInTeam: Int
    abstract fun enableTeamMode(teamSize: Int, indexInTeam: Int)
    abstract fun disableTeamMode()

    /* uploading, downloading */
    abstract val isAutoSync: StateFlow<Boolean>
    abstract val unsyncedEditsCount: StateFlow<Int>

    abstract val isUploading: StateFlow<Boolean>
    abstract val isUploadingOrDownloading: StateFlow<Boolean>

    abstract val isUserInitiatedDownloadInProgress: Boolean
    abstract val isLoggedIn: StateFlow<Boolean>
    abstract val isConnected: Boolean

    abstract fun upload()
    abstract fun download(bbox: BoundingBox)

    /* stars */
    abstract val starsCount: StateFlow<Int>
    abstract val isShowingStarsCurrentWeek: StateFlow<Boolean>
    abstract fun toggleShowingCurrentWeek()
}
