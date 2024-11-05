package de.westnordost.streetcomplete.screens.main

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.urlconfig.UrlConfig
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// not @Stable, as not all fields are StateFlows or immutable
abstract class MainViewModel : ViewModel() {
    /* error handling */
    abstract val lastCrashReport: StateFlow<String?>

    abstract val lastDownloadError: StateFlow<Exception?>
    abstract val lastUploadError: StateFlow<Exception?>
    abstract suspend fun createErrorReport(error: Exception): String

    /* start parameters */
    abstract fun setUri(uri: String)

    abstract val urlConfig: StateFlow<ShownUrlConfig?>
    abstract fun applyUrlConfig(config: UrlConfig)
    abstract val geoUri: StateFlow<CameraPosition?>

    /* intro */
    abstract var hasShownTutorial: Boolean

    /* messages */
    abstract val messagesCount: StateFlow<Int>
    abstract suspend fun popMessage(): Message?
    abstract val allQuestTypes: List<QuestType>

    /* overlays */
    abstract val selectedOverlay: StateFlow<Overlay?>
    abstract val overlays: List<Overlay>

    abstract var hasShownOverlaysTutorial: Boolean

    abstract fun selectOverlay(overlay: Overlay?)

    /* team mode */
    abstract val isTeamMode: StateFlow<Boolean>
    abstract var teamModeChanged: Boolean
    abstract val indexInTeam: StateFlow<Int>
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

    abstract val isRequestingLogin: StateFlow<Boolean>
    abstract fun finishRequestingLogin()

    abstract fun upload()
    abstract fun download(bbox: BoundingBox)

    /* stars */
    abstract val starsCount: StateFlow<Int>
    abstract val isShowingStarsCurrentWeek: StateFlow<Boolean>
    abstract fun toggleShowingCurrentWeek()

    /* map */
    // NOTE: currently filled from MainActivity (communication to compose view), i.e. the source of
    //       truth is actually the MapFragment
    abstract val locationState: MutableStateFlow<LocationState>
    abstract val mapCamera: MutableStateFlow<CameraPosition?>
    abstract val displayedPosition: MutableStateFlow<Offset?>

    abstract val isFollowingPosition: MutableStateFlow<Boolean>
    abstract val isNavigationMode: MutableStateFlow<Boolean>

    abstract val isRecordingTracks: MutableStateFlow<Boolean>
}

data class ShownUrlConfig(val urlConfig: UrlConfig, val alreadyExists: Boolean)
