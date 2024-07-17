package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.getIds
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/** Manages edit statistics - by element edit type and by country */
class StatisticsController(
    private val editTypeStatisticsDao: EditTypeStatisticsDao,
    private val countryStatisticsDao: CountryStatisticsDao,
    private val currentWeekEditTypeStatisticsDao: EditTypeStatisticsDao,
    private val currentWeekCountryStatisticsDao: CountryStatisticsDao,
    private val activeDatesDao: ActiveDatesDao,
    private val countryBoundaries: Lazy<CountryBoundaries>,
    private val prefs: Preferences
) : StatisticsSource {

    private val listeners = Listeners<StatisticsSource.Listener>()

    override var rank: Int
        get() = prefs.userGlobalRank
        private set(value) { prefs.userGlobalRank = value }

    override var daysActive: Int
        get() = prefs.userDaysActive
        private set(value) { prefs.userDaysActive = value }

    override var currentWeekRank: Int
        get() = prefs.userGlobalRankCurrentWeek
        private set(value) { prefs.userGlobalRankCurrentWeek = value }

    override var activeDatesRange: Int
        get() = prefs.userActiveDatesRange
        private set(value) { prefs.userActiveDatesRange = value }

    override var isSynchronizing: Boolean
        get() = prefs.isSynchronizingStatistics
        private set(value) { prefs.isSynchronizingStatistics = value }

    private var lastUpdate: Long
        get() = prefs.userLastTimestampActive
        set(value) { prefs.userLastTimestampActive = value }

    override fun getEditCount(): Int =
        editTypeStatisticsDao.getTotalAmount()

    override fun getEditTypeStatistics(): List<EditTypeStatistics> =
        editTypeStatisticsDao.getAll()

    override fun getEditCount(type: String): Int =
        editTypeStatisticsDao.getAmount(type)

    override fun getEditCount(types: List<String>): Int =
        editTypeStatisticsDao.getAmount(types)

    override fun getCountryStatistics(): List<CountryStatistics> =
        countryStatisticsDao.getAll()

    override fun getCountryStatisticsOfCountryWithBiggestSolvedCount() =
        countryStatisticsDao.getCountryWithBiggestSolvedCount()

    override fun getCurrentWeekEditCount(): Int =
        currentWeekEditTypeStatisticsDao.getTotalAmount()

    override fun getCurrentWeekEditTypeStatistics(): List<EditTypeStatistics> =
        currentWeekEditTypeStatisticsDao.getAll()

    override fun getCurrentWeekCountryStatistics(): List<CountryStatistics> =
        currentWeekCountryStatisticsDao.getAll()

    override fun getCurrentWeekCountryStatisticsOfCountryWithBiggestSolvedCount(): CountryStatistics? =
        currentWeekCountryStatisticsDao.getCountryWithBiggestSolvedCount()

    override fun getActiveDates(): List<LocalDate> =
        activeDatesDao.getAll(activeDatesRange)

    fun addOne(type: String, position: LatLon) {
        editTypeStatisticsDao.addOne(type)
        currentWeekEditTypeStatisticsDao.addOne(type)
        getRealCountryCode(position)?.let {
            countryStatisticsDao.addOne(it)
            currentWeekCountryStatisticsDao.addOne(it)
        }
        listeners.forEach { it.onAddedOne(type) }
        updateDaysActive()
    }

    fun subtractOne(type: String, position: LatLon) {
        editTypeStatisticsDao.subtractOne(type)
        currentWeekEditTypeStatisticsDao.subtractOne(type)
        getRealCountryCode(position)?.let {
            countryStatisticsDao.subtractOne(it)
            currentWeekCountryStatisticsDao.subtractOne(it)
        }
        listeners.forEach { it.onSubtractedOne(type) }
        updateDaysActive()
    }

    fun updateAll(statistics: Statistics) {
        val backendIsStillAnalyzing = statistics.isAnalyzing
        isSynchronizing = backendIsStillAnalyzing
        if (backendIsStillAnalyzing) {
            Log.i(TAG, "Backend is still analyzing changeset history")
            return
        }
        val backendDataIsUpToDate = statistics.lastUpdate / 1000 >= lastUpdate / 1000
        if (!backendDataIsUpToDate) {
            Log.i(TAG, "Backend data is not up-to-date")
            return
        }

        editTypeStatisticsDao.replaceAll(statistics.types.associate { it.type to it.count })
        countryStatisticsDao.replaceAll(statistics.countries)
        currentWeekEditTypeStatisticsDao.replaceAll(statistics.currentWeekTypes.associate { it.type to it.count })
        currentWeekCountryStatisticsDao.replaceAll(statistics.currentWeekCountries)
        currentWeekRank = statistics.currentWeekRank
        activeDatesDao.replaceAll(statistics.activeDates)
        rank = statistics.rank
        activeDatesRange = statistics.activeDatesRange
        daysActive = statistics.daysActive
        lastUpdate = statistics.lastUpdate

        listeners.forEach { it.onUpdatedAll() }
    }

    fun clear() {
        editTypeStatisticsDao.clear()
        countryStatisticsDao.clear()
        currentWeekEditTypeStatisticsDao.clear()
        currentWeekCountryStatisticsDao.clear()
        activeDatesDao.clear()
        prefs.clearUserStatistics()

        listeners.forEach { it.onCleared() }
    }

    private fun updateDaysActive() {
        val today = systemTimeNow().toLocalDate()
        val lastUpdateDate = Instant.fromEpochMilliseconds(lastUpdate).toLocalDate()
        lastUpdate = nowAsEpochMilliseconds()
        activeDatesDao.addToday()
        if (today > lastUpdateDate) {
            daysActive++
            listeners.forEach { it.onUpdatedDaysActive() }
        }
    }

    private fun getRealCountryCode(position: LatLon): String? =
        countryBoundaries.value.getIds(position).firstOrNull {
            // skip country subdivisions (e.g. US-TX)
            !it.contains('-')
        }

    override fun addListener(listener: StatisticsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: StatisticsSource.Listener) {
        listeners.remove(listener)
    }

    companion object {
        private const val TAG = "StatisticsController"
    }
}
