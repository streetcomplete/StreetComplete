package de.westnordost.streetcomplete.data.user.statistics

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.util.ktx.getIds
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask

/** Manages edit statistics - by element edit type and by country */
class StatisticsController(
    private val editTypeStatisticsDao: EditTypeStatisticsDao,
    private val countryStatisticsDao: CountryStatisticsDao,
    private val countryBoundaries: FutureTask<CountryBoundaries>,
    private val prefs: SharedPreferences,
    userLoginStatusSource: UserLoginStatusSource
) : StatisticsSource {

    private val listeners: MutableList<StatisticsSource.Listener> = CopyOnWriteArrayList()

    private val userLoginStatusListener = object : UserLoginStatusSource.Listener {
        override fun onLoggedIn() {}
        override fun onLoggedOut() {
            clear()
        }
    }

    override var rank: Int
        get() = prefs.getInt(Prefs.USER_GLOBAL_RANK, -1)
        private set(value) {
            prefs.edit(true) { putInt(Prefs.USER_GLOBAL_RANK, value) }
        }

    override var daysActive: Int
        get() = prefs.getInt(Prefs.USER_DAYS_ACTIVE, 0)
        private set(value) {
            prefs.edit(true) { putInt(Prefs.USER_DAYS_ACTIVE, value) }
        }

    override var isSynchronizing: Boolean
        // default true because if it is not set yet, the first thing that is done is to synchronize it
        get() = prefs.getBoolean(Prefs.IS_SYNCHRONIZING_STATISTICS, true)
        private set(value) {
            prefs.edit(true) { putBoolean(Prefs.IS_SYNCHRONIZING_STATISTICS, value) }
        }

    private var lastUpdate: Long
        get() = prefs.getLong(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 0)
        set(value) {
            prefs.edit(true) { putLong(Prefs.USER_LAST_TIMESTAMP_ACTIVE, value) }
        }

    init {
        userLoginStatusSource.addListener(userLoginStatusListener)
    }

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

    fun addOne(type: String, position: LatLon) {
        editTypeStatisticsDao.addOne(type)
        getRealCountryCode(position)?.let { countryStatisticsDao.addOne(it) }
        listeners.forEach { it.onAddedOne(type) }
        updateDaysActive()
    }

    fun subtractOne(type: String, position: LatLon) {
        editTypeStatisticsDao.subtractOne(type)
        getRealCountryCode(position)?.let { countryStatisticsDao.subtractOne(it) }
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
        rank = statistics.rank
        daysActive = statistics.daysActive
        lastUpdate = statistics.lastUpdate

        listeners.forEach { it.onUpdatedAll() }
    }

    private fun clear() {
        editTypeStatisticsDao.clear()
        countryStatisticsDao.clear()
        prefs.edit(true) {
            remove(Prefs.USER_DAYS_ACTIVE)
            remove(Prefs.IS_SYNCHRONIZING_STATISTICS)
            remove(Prefs.USER_GLOBAL_RANK)
            remove(Prefs.USER_LAST_TIMESTAMP_ACTIVE)
        }

        listeners.forEach { it.onCleared() }
    }

    private fun updateDaysActive() {
        val today = systemTimeNow().toLocalDate()
        val lastUpdateDate = Instant.fromEpochMilliseconds(lastUpdate).toLocalDate()
        lastUpdate = nowAsEpochMilliseconds()
        if (today > lastUpdateDate) {
            daysActive++
            listeners.forEach { it.onUpdatedDaysActive() }
        }
    }

    private fun getRealCountryCode(position: LatLon): String? =
        countryBoundaries.get().getIds(position).firstOrNull {
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
