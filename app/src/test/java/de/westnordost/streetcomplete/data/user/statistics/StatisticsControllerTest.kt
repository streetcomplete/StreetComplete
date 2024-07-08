package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import kotlinx.datetime.LocalDate
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticsControllerTest {

    private lateinit var editTypeStatisticsDao: EditTypeStatisticsDao
    private lateinit var countryStatisticsDao: CountryStatisticsDao
    private lateinit var currentWeekEditTypeStatisticsDao: EditTypeStatisticsDao
    private lateinit var currentWeekCountryStatisticsDao: CountryStatisticsDao
    private lateinit var activeDatesDao: ActiveDatesDao
    private lateinit var countryBoundaries: CountryBoundaries
    private lateinit var prefs: Preferences

    private lateinit var statisticsController: StatisticsController
    private lateinit var listener: StatisticsSource.Listener

    private val questA = "TestQuestTypeA"
    private val questB = "TestQuestTypeB"

    @BeforeTest fun setUp() {
        editTypeStatisticsDao = mock()
        countryStatisticsDao = mock()
        currentWeekEditTypeStatisticsDao = mock()
        currentWeekCountryStatisticsDao = mock()
        activeDatesDao = mock()
        countryBoundaries = mock()
        prefs = mock()
        listener = mock()

        statisticsController = StatisticsController(
            editTypeStatisticsDao, countryStatisticsDao,
            currentWeekEditTypeStatisticsDao, currentWeekCountryStatisticsDao,
            activeDatesDao,
            lazyOf(countryBoundaries), prefs
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
        on(prefs.userLastTimestampActive).thenReturn(0)

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
        on(prefs.userLastTimestampActive).thenReturn(0)

        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verify(editTypeStatisticsDao).subtractOne("TestQuestTypeA")
        verify(currentWeekEditTypeStatisticsDao).subtractOne("TestQuestTypeA")
        verify(activeDatesDao).addToday()
        verify(listener).onSubtractedOne(questA)
        verify(listener).onUpdatedDaysActive()
    }

    @Test fun `clear all`() {
        statisticsController.clear()
        verify(editTypeStatisticsDao).clear()
        verify(countryStatisticsDao).clear()
        verify(currentWeekCountryStatisticsDao).clear()
        verify(currentWeekEditTypeStatisticsDao).clear()
        verify(activeDatesDao).clear()
        verify(prefs).clearUserStatistics()
        verify(listener).onCleared()
    }

    @Test fun `update all`() {
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
        verify(prefs).userActiveDatesRange = 12
        verify(prefs).userDaysActive = 333
        verify(prefs).isSynchronizingStatistics = false
        verify(prefs).userGlobalRank = 999
        verify(prefs).userGlobalRankCurrentWeek = 111
        verify(prefs).userLastTimestampActive = 9999999
        verify(listener).onUpdatedAll()
    }
}
