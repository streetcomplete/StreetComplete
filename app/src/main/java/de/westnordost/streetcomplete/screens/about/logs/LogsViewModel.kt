package de.westnordost.streetcomplete.screens.about.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

abstract class LogsViewModel : ViewModel() {
    abstract val filters: StateFlow<LogsFilters>
    abstract val logs: StateFlow<List<LogMessage>>

    abstract fun setFilters(filters: LogsFilters)
}

class LogsViewModelImpl(
    private val logsController: LogsController,
) : LogsViewModel() {

    override val filters = MutableStateFlow(LogsFilters(
        timestampNewerThan = LocalDateTime(systemTimeNow().toLocalDate(), LocalTime(0, 0, 0))
    ))

    /**
     * Produce a call back flow of all incoming logs matching the given [filters].
     */
    private fun getIncomingLogs(filters: LogsFilters) = callbackFlow {
        // Listener that sends the messages matching the filters to the observer
        val listener = object : LogsController.Listener {
            override fun onAdded(message: LogMessage) {
                if (filters.matches(message)) {
                    trySend(message) // Send it to the observer
                }
            }
        }
        logsController.addListener(listener)
        awaitClose { logsController.removeListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val logs: StateFlow<List<LogMessage>> =
        filters.transformLatest { filters ->
            val logs = logsController.getLogs(filters).toMutableList()

            emit(UniqueList(logs))

            getIncomingLogs(filters).collect {
                logs.add(it)
                emit(UniqueList(logs))
            }
        }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, UniqueList(emptyList()))

    override fun setFilters(filters: LogsFilters) {
        this.filters.value = filters
    }
}

private fun LogsController.getLogs(filters: LogsFilters) =
    getLogs(
        levels = filters.levels,
        messageContains = filters.messageContains,
        newerThan = filters.timestampNewerThan?.toEpochMilli(),
        olderThan = filters.timestampOlderThan?.toEpochMilli()
    )

/** List that only returns true on equals if it is compared to the same instance */
// this is necessary so that Compose recognizes that the view should be updated after list changed
private class UniqueList<T>(private val list: List<T>) : List<T> by list {
    override fun equals(other: Any?) = this === other
    override fun hashCode() = list.hashCode()
}
