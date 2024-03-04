package de.westnordost.streetcomplete.screens.user.statistics

import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditStatisticsViewModelImpl(
    private val statisticsSource: StatisticsSource,
    private val allEditTypes: AllEditTypes,
) : EditStatisticsViewModel() {

    override val hasEdits = MutableStateFlow(true)
    override val isSynchronizingStatistics = MutableStateFlow(false)
    override val countryStatistics = MutableStateFlow<Collection<CountryStatistics>>(emptyList())
    override val editTypeObjStatistics = MutableStateFlow<Collection<EditTypeObjStatistics>>(emptyList())

    init {
        viewModelScope.launch {
            hasEdits.value = withContext(Dispatchers.IO) { statisticsSource.getEditCount() > 0 }
            isSynchronizingStatistics.value = statisticsSource.isSynchronizing
            countryStatistics.value = withContext(Dispatchers.IO) { statisticsSource.getCountryStatistics() }
            editTypeObjStatistics.value = withContext(Dispatchers.IO) { getEditTypeStatistics() }
        }
        // no updating of data implemented (because actually not needed. Not possible to add edits
        // while in this screen)
    }

    private fun getEditTypeStatistics(): Collection<EditTypeObjStatistics> =
        statisticsSource.getEditTypeStatistics().mapNotNull {
            val editType = allEditTypes.getByName(it.type) ?: return@mapNotNull null
            EditTypeObjStatistics(editType, it.count)
        }
}
