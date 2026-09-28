package de.westnordost.streetcomplete.screens.about.logs

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.data.logs.LogsSource
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime

@Stable
abstract class LogsViewModel : ViewModel() {
    /** Logs matching the given [filters], updated when new logs come in */
    abstract fun getLogs(filters: LogsFilters): Flow<List<LogMessage>>

    abstract suspend fun createLogsFile(logs: List<LogMessage>): PlatformFile
}

@Stable
class LogsViewModelImpl(
    private val logsSource: LogsSource,
) : LogsViewModel() {

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

    override fun getLogs(filters: LogsFilters): Flow<List<LogMessage>> = flow {
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
    }.flowOn(Dispatchers.IO)

    override suspend fun createLogsFile(logs: List<LogMessage>): PlatformFile {
        val logTimestamp = LocalDateTime.now().toString()
        val logTitle = "${ApplicationConstants.NAME}_${BuildConfig.VERSION_NAME}_$logTimestamp.log"
        val file = PlatformFile(FileKit.cacheDir, logTitle)
        withContext(Dispatchers.IO) {
            file.writeString(logs.format())
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
