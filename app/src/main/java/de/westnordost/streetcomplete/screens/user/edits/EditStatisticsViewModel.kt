package de.westnordost.streetcomplete.screens.user.edits

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.getYamlStringMap
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class EditStatisticsViewModel : ViewModel() {
    abstract val hasEdits: StateFlow<Boolean>
    abstract val isSynchronizingStatistics: StateFlow<Boolean>

    abstract val countryStatistics: StateFlow<List<CompleteCountryStatistics>?>
    abstract val editTypeStatistics: StateFlow<List<CompleteEditTypeStatistics>?>

    abstract fun queryCountryStatistics()
    abstract fun queryEditTypeStatistics()

    abstract val flagAlignments: StateFlow<Map<String, FlagAlignment>?>
}

data class CompleteEditTypeStatistics(
    val type: EditType,
    val count: Int,
    val countCurrentWeek: Int
)
data class CompleteCountryStatistics(
    val countryCode: String,
    val count: Int,
    val rank: Int?,
    val countCurrentWeek: Int,
    val rankCurrentWeek: Int?,
)

class EditStatisticsViewModelImpl(
    private val statisticsSource: StatisticsSource,
    private val allEditTypes: AllEditTypes,
    private val resources: Resources,
) : EditStatisticsViewModel() {

    override val hasEdits = MutableStateFlow(true)
    override val isSynchronizingStatistics = MutableStateFlow(statisticsSource.isSynchronizing)
    override val countryStatistics = MutableStateFlow<List<CompleteCountryStatistics>?>(null)
    override val editTypeStatistics = MutableStateFlow<List<CompleteEditTypeStatistics>?>(null)
    override val flagAlignments = MutableStateFlow<Map<String, FlagAlignment>?>(null)

    // no updating of data implemented (because actually not needed. Not possible to add edits
    // while in this screen)

    init {
        launch(IO) { hasEdits.value = statisticsSource.getEditCount() > 0 }
        launch(IO) {
            flagAlignments.value = resources
                .getYamlStringMap(R.raw.flag_alignments)
                .mapValues {
                    when (it.value) {
                        "left" ->         FlagAlignment.Left
                        "center-left" ->  FlagAlignment.CenterLeft
                        "center" ->       FlagAlignment.Center
                        "center-right" -> FlagAlignment.CenterRight
                        "right" ->        FlagAlignment.Right
                        "stretch" ->      FlagAlignment.Stretch
                        else ->           throw IllegalArgumentException()
                    }
                }
        }
    }

    override fun queryCountryStatistics() {
        if (countryStatistics.value == null) {
            launch(IO) {
                val statistics = statisticsSource.getCountryStatistics()
                    .associateBy { it.countryCode }

                val statisticsCurrentWeek = statisticsSource.getCurrentWeekCountryStatistics()
                    .associateBy { it.countryCode }

                countryStatistics.value = statistics
                    .map { (countryCode, stats) ->
                        val statsCurrentWeek = statisticsCurrentWeek[countryCode]
                        CompleteCountryStatistics(
                            countryCode,
                            stats.count,
                            stats.rank,
                            statsCurrentWeek?.count ?: 0,
                            statsCurrentWeek?.rank
                        )
                    }
                    .sortedByDescending { it.count }
            }
        }
    }

    override fun queryEditTypeStatistics() {
        if (editTypeStatistics.value == null) {
            launch(IO) {
                val statistics = statisticsSource.getEditTypeStatistics()
                    .associate { it.type to it.count }

                val statisticsCurrentWeek = statisticsSource.getCurrentWeekEditTypeStatistics()
                    .associate { it.type to it.count }

                editTypeStatistics.value = statistics
                    .mapNotNull { (type, count) ->
                        val editType = allEditTypes.getByName(type) ?: return@mapNotNull null
                        val countCurrentWeek = statisticsCurrentWeek[type] ?: 0
                        CompleteEditTypeStatistics(editType, count, countCurrentWeek)
                    }
                    .sortedByDescending { it.count }
            }
        }
    }
}
