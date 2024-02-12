package de.westnordost.streetcomplete.screens.about

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class LogsViewModel : ViewModel() {
    abstract val filters: MutableStateFlow<LogsFilters>
    abstract val logs: SharedFlow<List<LogMessage>>

    abstract val logsText: String
}
