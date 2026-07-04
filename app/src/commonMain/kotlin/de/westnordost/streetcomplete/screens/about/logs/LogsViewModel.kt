package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.data.logs.LogsSource
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.dialogs.shareFile
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.plus
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Stable
abstract class LogsViewModel : ViewModel() {
    abstract val filters: StateFlow<LogsFilters>
    abstract val logs: StateFlow<List<LogMessage>>

    abstract fun setFilters(filters: LogsFilters)

    abstract fun share()
}

@Stable
class LogsViewModelImpl(
    private val logsSource: LogsSource,
) : LogsViewModel() {

    override val filters = MutableStateFlow(LogsFilters(
        timestampNewerThan = LocalDateTime(systemTimeNow().toLocalDate(), LocalTime(0, 0, 0))
    ))

    /**
     * Produce a call back flow of all incoming logs matching the given [filters].
     */
    private fun getIncomingLogs(filters: LogsFilters) = callbackFlow {
        // Listener that sends the messages matching the filters to the observer
        val listener = object : LogsSource.Listener {
            override fun onAdded(message: LogMessage) {
                if (filters.matches(message)) {
                    trySend(message) // Send it to the observer
                }
            }
        }
        logsSource.addListener(listener)
        awaitClose { logsSource.removeListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val logs: StateFlow<List<LogMessage>> =
        filters.transformLatest { filters ->
            val logs = logsSource
                .getLogs(
                    levels = filters.levels,
                    messageContains = filters.messageContains,
                    newerThan = filters.timestampNewerThan?.toEpochMilli(),
                    olderThan = filters.timestampOlderThan?.toEpochMilli()
                )
                .toMutableList()

            emit(UniqueList(logs))

            getIncomingLogs(filters).collect {
                logs.add(it)
                emit(UniqueList(logs))
            }
        }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, UniqueList(emptyList()))

    override fun setFilters(filters: LogsFilters) {
        this.filters.value = filters
    }

    override fun share() {
        val logTimestamp = LocalDateTime.now().toString()
        val logTitle = "${ApplicationConstants.NAME}_${BuildConfig.VERSION_NAME}_$logTimestamp.log"
        val file = PlatformFile(FileKit.cacheDir, logTitle)
        launch {
            file.writeString(logs.value.format())
            FileKit.shareFile(file = file)
        }
    }
}

/** List that only returns true on equals if it is compared to the same instance */
// this is necessary so that Compose recognizes that the view should be updated after list changed
private class UniqueList<T>(private val list: List<T>) : List<T> by list {
    override fun equals(other: Any?) = this === other
    override fun hashCode() = list.hashCode()
}
