package de.westnordost.streetcomplete.screens.user.statistics

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class EditStatisticsViewModel : ViewModel() {
    abstract val hasEdits: StateFlow<Boolean>
    abstract val isSynchronizingStatistics: StateFlow<Boolean>
    abstract val countryStatistics: StateFlow<Collection<CountryStatistics>?>
    abstract val editTypeStatistics: StateFlow<Collection<EditTypeObjStatistics>?>

    abstract fun queryCountryStatistics()
    abstract fun queryEditTypeStatistics()
}

data class EditTypeObjStatistics(val type: EditType, val count: Int)

class EditStatisticsViewModelImpl(
    private val statisticsSource: StatisticsSource,
    private val allEditTypes: AllEditTypes,
) : EditStatisticsViewModel() {

    override val hasEdits = MutableStateFlow(true)
    override val isSynchronizingStatistics = MutableStateFlow(statisticsSource.isSynchronizing)
    override val countryStatistics = MutableStateFlow<Collection<CountryStatistics>?>(null)
    override val editTypeStatistics = MutableStateFlow<Collection<EditTypeObjStatistics>?>(null)

    // no updating of data implemented (because actually not needed. Not possible to add edits
    // while in this screen)

    init {
        launch(Dispatchers.IO) { hasEdits.value = statisticsSource.getEditCount() > 0 }
    }

    override fun queryCountryStatistics() {
        if (countryStatistics.value == null) {
            launch(Dispatchers.IO) { countryStatistics.value = statisticsSource.getCountryStatistics() }
        }
    }

    override fun queryEditTypeStatistics() {
        if (editTypeStatistics.value == null) {
            launch(Dispatchers.IO) { editTypeStatistics.value = getEditTypeStatistics() }
        }
    }

    private fun getEditTypeStatistics(): Collection<EditTypeObjStatistics> =
        statisticsSource.getEditTypeStatistics().mapNotNull {
            val editType = allEditTypes.getByName(it.type) ?: return@mapNotNull null
            EditTypeObjStatistics(editType, it.count)
        }
}
