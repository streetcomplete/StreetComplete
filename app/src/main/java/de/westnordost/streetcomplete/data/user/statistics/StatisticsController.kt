package de.westnordost.streetcomplete.data.user.statistics

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.util.ktx.getIds
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask

/** Manages statistics of solved quests - by quest type and by country */
class StatisticsController(
    private val questTypeStatisticsDao: QuestTypeStatisticsDao,
    private val countryStatisticsDao: CountryStatisticsDao,
    private val countryBoundaries: FutureTask<CountryBoundaries>,
    private val questTypeRegistry: QuestTypeRegistry,
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

    override fun getSolvedCount(): Int =
        questTypeStatisticsDao.getTotalAmount()

    override fun getQuestStatistics(): List<QuestTypeStatistics> =
        questTypeStatisticsDao.getAll().mapNotNull {
            val questType = questTypeRegistry.getByName(it.key)
            if (questType != null) QuestTypeStatistics(questType, it.value) else null
        }

    override fun getSolvedCount(questType: QuestType<*>): Int =
        questTypeStatisticsDao.getAmount(questType.name)

    override fun getSolvedCount(questTypes: List<QuestType<*>>): Int =
        questTypeStatisticsDao.getAmount(questTypes.map { it.name })

    override fun getCountryStatistics(): List<CountryStatistics> =
        countryStatisticsDao.getAll()

    override fun getCountryStatisticsOfCountryWithBiggestSolvedCount() =
        countryStatisticsDao.getCountryWithBiggestSolvedCount()

    fun addOne(questType: QuestType<*>, position: LatLon) {
        questTypeStatisticsDao.addOne(questType.name)
        getRealCountryCode(position)?.let { countryStatisticsDao.addOne(it) }
        listeners.forEach { it.onAddedOne(questType) }
        updateDaysActive()
    }

    fun subtractOne(questType: QuestType<*>, position: LatLon) {
        questTypeStatisticsDao.subtractOne(questType.name)
        getRealCountryCode(position)?.let { countryStatisticsDao.subtractOne(it) }
        listeners.forEach { it.onSubtractedOne(questType) }
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

        questTypeStatisticsDao.replaceAll(statistics.questTypes.associate { it.questType.name to it.solvedCount })
        countryStatisticsDao.replaceAll(statistics.countries)
        rank = statistics.rank
        daysActive = statistics.daysActive
        lastUpdate = statistics.lastUpdate

        listeners.forEach { it.onUpdatedAll() }
    }

    private fun clear() {
        questTypeStatisticsDao.clear()
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
        val today = LocalDate.now()
        val lastUpdateDate = Instant.ofEpochMilli(lastUpdate).toLocalDate()
        lastUpdate = Instant.now().toEpochMilli()
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

private val QuestType<*>.name get() = this::class.simpleName!!
