package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.data.logs.LogsSource
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

@Stable
abstract class LogsViewModel : ViewModel() {
    abstract val filters: StateFlow<LogsFilters>
    abstract val logs: StateFlow<List<LogMessage>>

    abstract fun setFilters(filters: LogsFilters)

    abstract suspend fun createLogsFile(): PlatformFile
}

@Stable
class LogsViewModelImpl(
    private val logsSource: LogsSource,
) : LogsViewModel() {

    override val filters = MutableStateFlow(LogsFilters(
        timestampNewerThan = LocalDateTime(systemTimeNow().toLocalDate(), LocalTime(0, 0, 0))
    ))

    @OptIn(ExperimentalCoroutinesApi::class)
    override val logs: StateFlow<List<LogMessage>> = filters
        .flatMapLatest { getLogs(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UniqueList(emptyList()))

    override fun setFilters(filters: LogsFilters) {
        this.filters.value = filters
    }

    /** Logs matching the given [filters], updated when new logs come in */
    private fun getLogs(filters: LogsFilters): Flow<List<LogMessage>> = callbackFlow {
        val logs = withContext(Dispatchers.IO) { logsSource
            .getLogs(
                levels = filters.levels,
                messageContains = filters.messageContains,
                newerThan = filters.timestampNewerThan?.toEpochMilli(),
                olderThan = filters.timestampOlderThan?.toEpochMilli()
            )
            .toMutableList()
        }

        trySend(UniqueList(logs))

        val listener = object : LogsSource.Listener {
            override fun onAdded(message: LogMessage) {
                if (filters.matches(message)) {
                    logs.add(message)
                    trySend(UniqueList(logs))
                }
            }
        }

        logsSource.addListener(listener)
        awaitClose { logsSource.removeListener(listener) }
    }

    override suspend fun createLogsFile(): PlatformFile {
        val logTimestamp = LocalDateTime.now().toString()
        val logTitle = "${ApplicationConstants.NAME}_${BuildConfig.VERSION_NAME}_$logTimestamp.log"
        val file = PlatformFile(FileKit.cacheDir, logTitle)
        withContext(Dispatchers.IO) {
            file.writeString(logs.value.format())
        }
        return file
    }
}

/** List that only returns true on equals if it is compared to the same instance */
// this is necessary so that Compose recognizes that the view should be updated after list changed
private class UniqueList<T>(private val list: List<T>) : List<T> by list {
    override fun equals(other: Any?) = this === other
    override fun hashCode() = list.hashCode()
}
