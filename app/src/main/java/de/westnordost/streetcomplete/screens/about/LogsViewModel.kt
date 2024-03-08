package de.westnordost.streetcomplete.screens.about

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class LogsViewModel : ViewModel() {
    abstract val filters: StateFlow<LogsFilters>
    abstract val logs: StateFlow<List<LogMessage>>

    abstract fun setFilters(filters: LogsFilters)
}
