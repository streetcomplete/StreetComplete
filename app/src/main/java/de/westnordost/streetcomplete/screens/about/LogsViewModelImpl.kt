package de.westnordost.streetcomplete.screens.about

import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class LogsViewModelImpl(
    private val logsController: LogsController
) : LogsViewModel() {

    private val logsControllerListener = object : LogsController.Listener {
        override fun onAdded(message: LogMessage) {
            if (filters.value.matches(message)) {
                synchronized(_logs) {
                    _logs.add(message)
                    logs.tryEmit(_logs)
                }
            }
        }
    }

    override val filters: MutableStateFlow<LogsFilters>

    override val logs: MutableSharedFlow<List<LogMessage>> = MutableSharedFlow(1)
    private val _logs = ArrayList<LogMessage>()

    override val logsText: String get() = _logs.format()

    init {
        val startOfToday = LocalDateTime(systemTimeNow().toLocalDate(), LocalTime(0, 0, 0))
        filters = MutableStateFlow(LogsFilters(timestampNewerThan = startOfToday))
        viewModelScope.launch {
            // get logs initially, subscribe to updates and then...
            updateLogs(filters.value)
            logsController.addListener(logsControllerListener)

            // get logs anew whenever filters changed
            filters.collect { f -> updateLogs(f) }
        }
    }

    private suspend fun updateLogs(filters: LogsFilters) {
        val newLogs = withContext(Dispatchers.IO) { logsController.getLogs(filters) }
        synchronized(_logs) {
            _logs.clear()
            _logs.addAll(newLogs)
            logs.tryEmit(_logs)
        }
    }

    override fun onCleared() {
        logsController.removeListener(logsControllerListener)
    }
}

private fun LogsController.getLogs(filters: LogsFilters) =
    getLogs(
        levels = filters.levels,
        messageContains = filters.messageContains,
        newerThan = filters.timestampNewerThan?.toEpochMilli(),
        olderThan = filters.timestampOlderThan?.toEpochMilli()
    )
