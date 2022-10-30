package de.westnordost.streetcomplete.data.user.statistics

import android.content.SharedPreferences
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.verify
import java.util.concurrent.FutureTask

class StatisticsControllerTest {

    private lateinit var editTypeStatisticsDao: EditTypeStatisticsDao
    private lateinit var countryStatisticsDao: CountryStatisticsDao
    private lateinit var currentWeekEditTypeStatisticsDao: EditTypeStatisticsDao
    private lateinit var currentWeekCountryStatisticsDao: CountryStatisticsDao
    private lateinit var activeDatesDao: ActiveDatesDao
    private lateinit var countryBoundaries: CountryBoundaries
    private lateinit var prefs: SharedPreferences
    private lateinit var loginStatusSource: UserLoginStatusSource
    private lateinit var loginStatusListener: UserLoginStatusSource.Listener

    private lateinit var statisticsController: StatisticsController
    private lateinit var listener: StatisticsSource.Listener

    private val questA = "TestQuestTypeA"
    private val questB = "TestQuestTypeB"

    @Before fun setUp() {
        editTypeStatisticsDao = mock()
        countryStatisticsDao = mock()
        currentWeekEditTypeStatisticsDao = mock()
        currentWeekCountryStatisticsDao = mock()
        activeDatesDao = mock()
        countryBoundaries = mock()
        prefs = mock()
        on(prefs.edit()).thenReturn(mock())
        listener = mock()
        loginStatusSource = mock()

        val ft = FutureTask({}, countryBoundaries)
        ft.run()

        on(loginStatusSource.addListener(any())).then { invocation ->
            loginStatusListener = invocation.getArgument(0)
            Unit
        }

        statisticsController = StatisticsController(
            editTypeStatisticsDao, countryStatisticsDao,
            currentWeekEditTypeStatisticsDao, currentWeekCountryStatisticsDao,
            activeDatesDao,
            ft, prefs, loginStatusSource
        )
        statisticsController.addListener(listener)
    }

    @Test fun getSolvedCount() {
        on(editTypeStatisticsDao.getTotalAmount()).thenReturn(5)
        assertEquals(
            5,
            statisticsController.getEditCount()
        )
    }

    @Test fun `adding one`() {
        on(countryBoundaries.getIds(anyDouble(), anyDouble())).thenReturn(listOf("US-TX", "US", "World"))
        statisticsController.addOne(questA, p(0.0, 0.0))

        verify(editTypeStatisticsDao).addOne("TestQuestTypeA")
        verify(countryStatisticsDao).addOne("US")
        verify(currentWeekEditTypeStatisticsDao).addOne("TestQuestTypeA")
        verify(currentWeekCountryStatisticsDao).addOne("US")
        verify(activeDatesDao).addToday()
        verify(listener).onAddedOne(questA)
    }

    @Test fun `adding one one day later`() {
        on(prefs.getInt(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 0)).thenReturn(0)

        statisticsController.addOne(questA, p(0.0, 0.0))

        verify(editTypeStatisticsDao).addOne("TestQuestTypeA")
        verify(currentWeekEditTypeStatisticsDao).addOne("TestQuestTypeA")
        verify(activeDatesDao).addToday()
        verify(listener).onAddedOne(questA)
        verify(listener).onUpdatedDaysActive()
    }

    @Test fun `subtracting one`() {
        on(countryBoundaries.getIds(anyDouble(), anyDouble())).thenReturn(listOf("US-TX", "US", "World"))
        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verify(editTypeStatisticsDao).subtractOne("TestQuestTypeA")
        verify(countryStatisticsDao).subtractOne("US")
        verify(currentWeekEditTypeStatisticsDao).subtractOne("TestQuestTypeA")
        verify(currentWeekCountryStatisticsDao).subtractOne("US")
        verify(activeDatesDao).addToday()
        verify(listener).onSubtractedOne(questA)
    }

    @Test fun `subtracting one one day later`() {
        on(prefs.getInt(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 0)).thenReturn(0)

        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verify(editTypeStatisticsDao).subtractOne("TestQuestTypeA")
        verify(currentWeekEditTypeStatisticsDao).subtractOne("TestQuestTypeA")
        verify(activeDatesDao).addToday()
        verify(listener).onSubtractedOne(questA)
        verify(listener).onUpdatedDaysActive()
    }

    @Test fun `clear all`() {
        val editor: SharedPreferences.Editor = mock()
        on(prefs.edit()).thenReturn(editor)

        loginStatusListener.onLoggedOut()

        verify(editTypeStatisticsDao).clear()
        verify(countryStatisticsDao).clear()
        verify(currentWeekCountryStatisticsDao).clear()
        verify(currentWeekEditTypeStatisticsDao).clear()
        verify(activeDatesDao).clear()
        verify(editor).remove(Prefs.USER_DAYS_ACTIVE)
        verify(editor).remove(Prefs.IS_SYNCHRONIZING_STATISTICS)
        verify(editor).remove(Prefs.USER_GLOBAL_RANK)
        verify(editor).remove(Prefs.USER_GLOBAL_RANK_CURRENT_WEEK)
        verify(editor).remove(Prefs.USER_LAST_TIMESTAMP_ACTIVE)
        verify(listener).onCleared()
    }

    @Test fun `update all`() {
        val editor: SharedPreferences.Editor = mock()
        on(prefs.edit()).thenReturn(editor)

        statisticsController.updateAll(Statistics(
            types = listOf(
                EditTypeStatistics(questA, 123),
                EditTypeStatistics(questB, 44),
            ),
            countries = listOf(
                CountryStatistics("DE", 12, 5),
                CountryStatistics("US", 43, null),
            ),
            rank = 999,
            daysActive = 333,
            currentWeekRank = 111,
            currentWeekTypes = listOf(
                EditTypeStatistics(questA, 321),
                EditTypeStatistics(questB, 33),
            ),
            currentWeekCountries = listOf(
                CountryStatistics("AT", 999, 88),
                CountryStatistics("IT", 99, null),
            ),
            activeDatesRange = 12,
            activeDates = listOf(
                LocalDate.parse("1999-04-08"),
                LocalDate.parse("1888-01-02"),
            ),
            lastUpdate = 9999999,
            isAnalyzing = false
        ))
        verify(editTypeStatisticsDao).replaceAll(mapOf(
            "TestQuestTypeA" to 123,
            "TestQuestTypeB" to 44
        ))
        verify(countryStatisticsDao).replaceAll(listOf(
            CountryStatistics("DE", 12, 5),
            CountryStatistics("US", 43, null),
        ))
        verify(currentWeekEditTypeStatisticsDao).replaceAll(mapOf(
            "TestQuestTypeA" to 321,
            "TestQuestTypeB" to 33
        ))
        verify(currentWeekCountryStatisticsDao).replaceAll(listOf(
            CountryStatistics("AT", 999, 88),
            CountryStatistics("IT", 99, null),
        ))
        verify(activeDatesDao).replaceAll(listOf(
            LocalDate.parse("1999-04-08"),
            LocalDate.parse("1888-01-02")
        ))
        verify(editor).putInt(Prefs.ACTIVE_DATES_RANGE, 12)
        verify(editor).putInt(Prefs.USER_DAYS_ACTIVE, 333)
        verify(editor).putBoolean(Prefs.IS_SYNCHRONIZING_STATISTICS, false)
        verify(editor).putInt(Prefs.USER_GLOBAL_RANK, 999)
        verify(editor).putInt(Prefs.USER_GLOBAL_RANK_CURRENT_WEEK, 111)
        verify(editor).putLong(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 9999999)
        verify(listener).onUpdatedAll()
    }
}
