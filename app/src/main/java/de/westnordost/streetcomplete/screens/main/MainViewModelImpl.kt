package de.westnordost.streetcomplete.screens.main

import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.messages.MessagesSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.platform.InternetConnectionState
import de.westnordost.streetcomplete.data.preferences.Autosync
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

class MainViewModelImpl(
    private val uploadController: UploadController,
    private val uploadProgressSource: UploadProgressSource,
    private val downloadController: DownloadController,
    private val downloadProgressSource: DownloadProgressSource,
    private val userLoginSource: UserLoginSource,
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
    private val statisticsSource: StatisticsSource,
    private val internetConnectionState: InternetConnectionState,
    private val selectedOverlayController: SelectedOverlayController,
    private val overlayRegistry: OverlayRegistry,
    private val messagesSource: MessagesSource,
    private val teamModeQuestFilter: TeamModeQuestFilter,
    private val prefs: Preferences,
) : MainViewModel() {

    /* messages */

    override val messagesCount: StateFlow<Int> = callbackFlow {
        send(messagesSource.getNumberOfMessages())
        val listener = object : MessagesSource.UpdateListener {
            override fun onNumberOfMessagesUpdated(messageCount: Int) { trySend(messageCount) }
        }
        messagesSource.addListener(listener)
        awaitClose { messagesSource.removeListener(listener) }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, 0)

    override suspend fun popMessage(): Message? = withContext(Dispatchers.IO) {
        messagesSource.popNextMessage()
    }

    /* overlays */

    override val overlays: List<Overlay> get() = overlayRegistry

    override val selectedOverlay: StateFlow<Overlay?> = callbackFlow {
        send(selectedOverlayController.selectedOverlay)
        val listener = object : SelectedOverlaySource.Listener {
            override fun onSelectedOverlayChanged() {
                trySend(selectedOverlayController.selectedOverlay)
            }
        }
        selectedOverlayController.addListener(listener)
        awaitClose { selectedOverlayController.removeListener(listener) }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, null)

    override val hasShownOverlaysTutorial: Boolean get() =
        prefs.hasShownOverlaysTutorial

    override fun selectOverlay(overlay: Overlay?) {
        launch(Dispatchers.IO) {
            selectedOverlayController.selectedOverlay = overlay
        }
    }

    /* team mode */

    override val isTeamMode = MutableStateFlow(teamModeQuestFilter.isEnabled)

    override val indexInTeam: Int
        get() = teamModeQuestFilter.indexInTeam

    override var teamModeChanged: Boolean = false

    override fun enableTeamMode(teamSize: Int, indexInTeam: Int) {
        launch(Dispatchers.IO) { teamModeQuestFilter.enableTeamMode(teamSize, indexInTeam) }
    }

    override fun disableTeamMode() {
        launch(Dispatchers.IO) { teamModeQuestFilter.disableTeamMode() }
    }

    override fun download(bbox: BoundingBox) {
        downloadController.download(bbox, true)
    }

    private val teamModeListener = object : TeamModeQuestFilter.TeamModeChangeListener {
        override fun onTeamModeChanged(enabled: Boolean) {
            teamModeChanged = true
            isTeamMode.value = enabled
        }
    }

    /* uploading, downloading */

    override val isAutoSync: StateFlow<Boolean> = callbackFlow {
        send(prefs.autosync == Autosync.ON)
        val listener = prefs.onAutosyncChanged { trySend(it == Autosync.ON) }
        awaitClose { listener.deactivate() }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, true)

    override val unsyncedEditsCount: StateFlow<Int> = callbackFlow {
        var count = unsyncedChangesCountSource.getCount()
        send(count)
        val listener = object : UnsyncedChangesCountSource.Listener {
            override fun onIncreased() { trySend(++count) }
            override fun onDecreased() { trySend(--count) }
        }
        unsyncedChangesCountSource.addListener(listener)
        awaitClose { unsyncedChangesCountSource.removeListener(listener) }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, 0)

    override val isUploading: StateFlow<Boolean> = callbackFlow {
        val listener = object : UploadProgressSource.Listener {
            override fun onStarted() { trySend(true) }
            override fun onFinished() { trySend(false) }
        }
        uploadProgressSource.addListener(listener)
        awaitClose { uploadProgressSource.removeListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, uploadProgressSource.isUploadInProgress)

    private val isDownloading: StateFlow<Boolean> = callbackFlow<Boolean> {
        val listener = object : DownloadProgressSource.Listener {
            override fun onStarted() { trySend(true) }
            override fun onFinished() { trySend(false) }
        }
        downloadProgressSource.addListener(listener)
        awaitClose { downloadProgressSource.removeListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, downloadProgressSource.isDownloadInProgress)

    override val isUploadingOrDownloading: StateFlow<Boolean> =
        combine(isUploading, isDownloading) { it1, it2 -> it1 || it2 }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override val isUserInitiatedDownloadInProgress: Boolean
        get() = downloadProgressSource.isUserInitiatedDownloadInProgress

    override var isLoggedIn: StateFlow<Boolean> = callbackFlow {
        send(userLoginSource.isLoggedIn)
        val listener = object : UserLoginSource.Listener {
            override fun onLoggedIn() { trySend(true) }
            override fun onLoggedOut() { trySend(false) }
        }
        userLoginSource.addListener(listener)
        awaitClose { userLoginSource.removeListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override val isConnected: Boolean get() = internetConnectionState.isConnected

    override fun upload() {
        uploadController.upload(isUserInitiated = true)
    }

    /* stars */

    private val editCount: Flow<Int> = callbackFlow {
        var count = statisticsSource.getEditCount()
        send(count)

        fun update() {
            count = statisticsSource.getEditCount()
            trySend(count)
        }

        val listener = object : StatisticsSource.Listener {
            override fun onAddedOne(type: String) { trySend(++count) }
            override fun onSubtractedOne(type: String) { trySend(--count) }
            override fun onUpdatedAll() { update() }
            override fun onCleared() { update() }
            override fun onUpdatedDaysActive() {}
        }
        statisticsSource.addListener(listener)
        awaitClose { statisticsSource.removeListener(listener) }
    }

    private val editCountCurrentWeek: Flow<Int> = callbackFlow {
        var count = statisticsSource.getCurrentWeekEditCount()
        send(count)

        fun update() {
            count = statisticsSource.getCurrentWeekEditCount()
            trySend(count)
        }

        val listener = object : StatisticsSource.Listener {
            override fun onAddedOne(type: String) { trySend(++count) }
            override fun onSubtractedOne(type: String) { trySend(--count) }
            override fun onUpdatedAll() { update() }
            override fun onCleared() { update() }
            override fun onUpdatedDaysActive() {}
        }
        statisticsSource.addListener(listener)
        awaitClose { statisticsSource.removeListener(listener) }
    }

    private val solvedEditsCount: Flow<Int> = callbackFlow {
        send(unsyncedChangesCountSource.getSolvedCount())
        val listener = object : UnsyncedChangesCountSource.Listener {
            // always must recalculate / fetch new from db because not all unsynced edits count
            // for the stars. E.g. note edits don't count
            override fun onIncreased() { trySend(unsyncedChangesCountSource.getSolvedCount()) }
            override fun onDecreased() { trySend(unsyncedChangesCountSource.getSolvedCount()) }
        }
        unsyncedChangesCountSource.addListener(listener)
        awaitClose { unsyncedChangesCountSource.removeListener(listener) }
    }

    override val isShowingStarsCurrentWeek = MutableStateFlow(false)

    override fun toggleShowingCurrentWeek() {
        isShowingStarsCurrentWeek.update { !it }
    }

    override val starsCount: StateFlow<Int> = combine(
        editCount, editCountCurrentWeek, solvedEditsCount, isAutoSync, isShowingStarsCurrentWeek
    ) { editCount, editCountCurrentWeek, solvedEditsCount, isAutoSync, isShowingStarsCurrentWeek ->
        // when autosync is off, the unsynced edits are instead shown on the download button
        val unsyncedEdits = if (isAutoSync) solvedEditsCount else 0
        val syncedEdits = if (isShowingStarsCurrentWeek) editCountCurrentWeek else editCount
        syncedEdits + unsyncedEdits
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, 0)

    // ---------------------------------------------------------------------------------------

    init {
        teamModeQuestFilter.addListener(teamModeListener)
    }

    override fun onCleared() {
        teamModeQuestFilter.removeListener(teamModeListener)
    }
}
