package de.westnordost.streetcomplete.screens.user.statistics

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import kotlinx.coroutines.flow.StateFlow

abstract class EditStatisticsViewModel: ViewModel() {
    abstract val hasEdits: StateFlow<Boolean>
    abstract val isSynchronizingStatistics: StateFlow<Boolean>
    abstract val countryStatistics: StateFlow<Collection<CountryStatistics>>
    abstract val editTypeStatistics: StateFlow<Collection<EditTypeObjStatistics>>

    abstract fun queryCountryStatistics()
    abstract fun queryEditTypeStatistics()
}

data class EditTypeObjStatistics(val type: EditType, val count: Int)
