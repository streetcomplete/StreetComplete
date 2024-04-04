package de.westnordost.streetcomplete.screens.main.controls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus

abstract class AnswersCounterViewModel : ViewModel() {
    abstract val isUploadingOrDownloading: StateFlow<Boolean>

    abstract val answersCount: StateFlow<Int>

    abstract val isShowingCurrentWeek: StateFlow<Boolean>
    abstract fun toggleShowingCurrentWeek()
}

class AnswersCounterViewModelImpl(
    private val uploadProgressSource: UploadProgressSource,
    private val downloadProgressSource: DownloadProgressSource,
    private val prefs: ObservableSettings,
    private val statisticsSource: StatisticsSource,
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
) : AnswersCounterViewModel() {

    override val isUploadingOrDownloading = MutableStateFlow(
        uploadProgressSource.isUploadInProgress || downloadProgressSource.isDownloadInProgress
    )

    override val isShowingCurrentWeek = MutableStateFlow(false)

    private val editCount = MutableStateFlow<Int>(0)
    private val editCountCurrentWeek = MutableStateFlow<Int>(0)
    private val unsyncedEditsCount = MutableStateFlow<Int>(0)
    private val isAutoSync = callbackFlow<Boolean> {
        send(isAutoSync(prefs.getStringOrNull(Prefs.AUTOSYNC)))
        val listener = prefs.addStringOrNullListener(Prefs.AUTOSYNC) { isAutoSync(it) }
        awaitClose { listener.deactivate() }
    }

    override val answersCount: StateFlow<Int> = combine(
        editCount, editCountCurrentWeek, unsyncedEditsCount, isAutoSync, isShowingCurrentWeek
    ) { editCount, editCountCurrentWeek, unsyncedEditsCount, isAutoSync, isShowingCurrentWeek ->
        // when autosync is off, the unsynced edits are instead shown on the download button
        val unsyncedEdits = if (isAutoSync) unsyncedEditsCount else 0
        val syncedEdits = if (isShowingCurrentWeek) editCountCurrentWeek else editCount
        syncedEdits + unsyncedEdits
    }.stateIn(viewModelScope + IO, SharingStarted.Lazily, 0)

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { unsyncedEditsCount.update { it + 1 } }
        override fun onDecreased() { unsyncedEditsCount.update { it - 1 } }
    }

    private val statisticsListener = object : StatisticsSource.Listener {
        override fun onAddedOne(type: String) { changeEditCount(+1) }
        override fun onSubtractedOne(type: String) { changeEditCount(-1) }
        override fun onUpdatedAll() { updateEditCount() }
        override fun onCleared() { updateEditCount() }
        override fun onUpdatedDaysActive() {}
    }

    private val uploadProgressListener = object : UploadProgressSource.Listener {
        override fun onStarted() { updateUploadOrDownloadInProgress() }
        override fun onFinished() { updateUploadOrDownloadInProgress() }
    }

    private val downloadProgressListener = object : DownloadProgressSource.Listener {
        override fun onStarted() { updateUploadOrDownloadInProgress() }
        override fun onFinished() { updateUploadOrDownloadInProgress() }
    }

    init {
        updateEditCount()
        updateUnsyncedChangesCount()

        uploadProgressSource.addListener(uploadProgressListener)
        downloadProgressSource.addListener(downloadProgressListener)
        statisticsSource.addListener(statisticsListener)
        unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)
    }

    override fun onCleared() {
        uploadProgressSource.removeListener(uploadProgressListener)
        downloadProgressSource.removeListener(downloadProgressListener)
        statisticsSource.removeListener(statisticsListener)
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
    }

    override fun toggleShowingCurrentWeek() {
        isShowingCurrentWeek.update { !it }
    }

    private fun updateUploadOrDownloadInProgress() {
        isUploadingOrDownloading.value =
            uploadProgressSource.isUploadInProgress || downloadProgressSource.isDownloadInProgress
    }

    private fun updateEditCount() {
        launch(IO) {
            editCount.value = statisticsSource.getEditCount()
            editCountCurrentWeek.value = statisticsSource.getCurrentWeekEditCount()
        }
    }

    private fun updateUnsyncedChangesCount() {
        launch(IO) {
            unsyncedEditsCount.value = unsyncedChangesCountSource.getSolvedCount()
        }
    }

    private fun changeEditCount(by: Int) {
        editCount.update { it + by }
        editCountCurrentWeek.update { it + by }
    }

    private fun isAutoSync(pref: String?): Boolean =
        Prefs.Autosync.valueOf(pref ?: ApplicationConstants.DEFAULT_AUTOSYNC) == Prefs.Autosync.ON

}
