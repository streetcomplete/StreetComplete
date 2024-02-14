package de.westnordost.streetcomplete.screens.about

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

class LogsViewModelImpl(
    private val logsController: LogsController,
) : LogsViewModel() {

    override val filters: MutableStateFlow<LogsFilters> =
        MutableStateFlow(
            LogsFilters(
                timestampNewerThan = LocalDateTime(
                    systemTimeNow().toLocalDate(),
                    LocalTime(0, 0, 0)
                )
            )
        )

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

        // Start listening
        logsController.addListener(listener)

        // When there are no observers, stop listening.
        awaitClose { logsController.removeListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val logs: StateFlow<List<LogMessage>> =
        filters.transformLatest { filters ->
            // get prior logs into a backing state
            // There will be duplication regardless.
            val logs = ArrayList(logsController.getLogs(filters))

            // emit the logs for the first view
            emit(logs)

            // start listening to new logs
            getIncomingLogs(filters).collect {
                logs.add(it)
                emit(logs)
            }
        }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, emptyList())
}

private fun LogsController.getLogs(filters: LogsFilters) =
    getLogs(
        levels = filters.levels,
        messageContains = filters.messageContains,
        newerThan = filters.timestampNewerThan?.toEpochMilli(),
        olderThan = filters.timestampOlderThan?.toEpochMilli()
    )
