package de.westnordost.streetcomplete.screens.about

import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
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
                // TODO this is hugely inefficient (log list is copied every time a single entry is added!!!)
                logs.update { it + message }
            }
        }
    }

    override val filters: MutableStateFlow<LogsFilters> =
        MutableStateFlow(
            LogsFilters(
                timestampNewerThan = LocalDateTime(
                    systemTimeNow().toLocalDate(),
                    LocalTime(0, 0, 0)
                )
            )
        )

    override val logs: MutableStateFlow<List<LogMessage>> = MutableStateFlow(emptyList())

    init {
        viewModelScope.launch {
            // get logs initially and subscribe to updates, then
            logs.value = withContext(Dispatchers.IO) { logsController.getLogs(filters.value) }
            logsController.addListener(logsControllerListener)

            // get logs anew whenever filters changed
            filters.collect { f ->
                logs.update { withContext(Dispatchers.IO) { logsController.getLogs(f) } }
            }
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
