package de.westnordost.streetcomplete.screens.user.statistics

import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow

class EditStatisticsViewModelImpl(
    private val statisticsSource: StatisticsSource,
    private val allEditTypes: AllEditTypes,
) : EditStatisticsViewModel() {

    override val hasEdits = MutableStateFlow(true)
    override val isSynchronizingStatistics = MutableStateFlow(false)
    override val countryStatistics = MutableStateFlow<Collection<CountryStatistics>>(emptyList())
    override val editTypeStatistics = MutableStateFlow<Collection<EditTypeObjStatistics>>(emptyList())

    // no updating of data implemented (because actually not needed. Not possible to add edits
    // while in this screen)

    init {
        isSynchronizingStatistics.value = statisticsSource.isSynchronizing
        launch(IO) { hasEdits.value = statisticsSource.getEditCount() > 0 }
    }

    override fun queryCountryStatistics() {
        launch(IO) { countryStatistics.value = statisticsSource.getCountryStatistics() }
    }

    override fun queryEditTypeStatistics() {
        launch(IO) { editTypeStatistics.value = getEditTypeStatistics() }
    }

    private fun getEditTypeStatistics(): Collection<EditTypeObjStatistics> =
        statisticsSource.getEditTypeStatistics().mapNotNull {
            val editType = allEditTypes.getByName(it.type) ?: return@mapNotNull null
            EditTypeObjStatistics(editType, it.count)
        }
}
