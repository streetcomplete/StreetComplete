package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.user.UserLoginSource
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.p
import dev.mokkery.answering.calls
import kotlinx.datetime.LocalDate
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticsControllerImplTest {

    private lateinit var editTypeStatisticsDao: EditTypeStatisticsDao
    private lateinit var countryStatisticsDao: CountryStatisticsDao
    private lateinit var currentWeekEditTypeStatisticsDao: EditTypeStatisticsDao
    private lateinit var currentWeekCountryStatisticsDao: CountryStatisticsDao
    private lateinit var activeDatesDao: ActiveDatesDao
    private lateinit var countryBoundaries: CountryBoundaries
    private lateinit var prefs: Preferences
    private lateinit var userLoginSource: UserLoginSource
    private lateinit var userLoginListener: UserLoginSource.Listener

    private lateinit var statisticsController: StatisticsControllerImpl
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

        userLoginSource = mock() {
            every { addListener(any()) } calls { (listener: UserLoginSource.Listener) ->
                userLoginListener = listener
            }
        }


        statisticsController = StatisticsControllerImpl(
            editTypeStatisticsDao,
            countryStatisticsDao,
            currentWeekEditTypeStatisticsDao,
            currentWeekCountryStatisticsDao,
            activeDatesDao,
            lazyOf(countryBoundaries),
            prefs,
            userLoginSource
        )
        statisticsController.addListener(listener)
    }

    @Test fun getSolvedCount() {
        every { editTypeStatisticsDao.getTotalAmount() } returns 5
        assertEquals(
            5,
            statisticsController.getEditCount()
        )
    }

    @Test fun `adding one`() {
        every { countryBoundaries.getIds(any()) } returns listOf("US-TX", "US", "World")
        statisticsController.addOne(questA, p(0.0, 0.0))

        verify { editTypeStatisticsDao.addOne("TestQuestTypeA") }
        verify { countryStatisticsDao.addOne("US") }
        verify { currentWeekEditTypeStatisticsDao.addOne("TestQuestTypeA") }
        verify { currentWeekCountryStatisticsDao.addOne("US") }
        verify { activeDatesDao.addToday() }
        verify { listener.onAddedOne(questA) }
    }

    @Test fun `adding one one day later`() {
        every { prefs.userLastTimestampActive } returns 0

        statisticsController.addOne(questA, p(0.0, 0.0))

        verify { editTypeStatisticsDao.addOne("TestQuestTypeA") }
        verify { currentWeekEditTypeStatisticsDao.addOne("TestQuestTypeA") }
        verify { activeDatesDao.addToday() }
        verify { listener.onAddedOne(questA) }
        verify { listener.onUpdatedDaysActive() }
    }

    @Test fun `subtracting one`() {
        every { countryBoundaries.getIds(any()) } returns listOf("US-TX", "US", "World")
        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verify { editTypeStatisticsDao.subtractOne("TestQuestTypeA") }
        verify { countryStatisticsDao.subtractOne("US") }
        verify { currentWeekEditTypeStatisticsDao.subtractOne("TestQuestTypeA") }
        verify { currentWeekCountryStatisticsDao.subtractOne("US") }
        verify { activeDatesDao.addToday() }
        verify { listener.onSubtractedOne(questA) }
    }

    @Test fun `subtracting one one day later`() {
        every { prefs.userLastTimestampActive } returns 0

        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verify { editTypeStatisticsDao.subtractOne("TestQuestTypeA") }
        verify { currentWeekEditTypeStatisticsDao.subtractOne("TestQuestTypeA") }
        verify { activeDatesDao.addToday() }
        verify { listener.onSubtractedOne(questA) }
        verify { listener.onUpdatedDaysActive() }
    }

    @Test fun `mark as not synchronized on login`() {
        userLoginListener.onLoggedIn()
        verify { prefs.statisticsSynchronizedOnce = false }    }

    @Test fun `clear on logout`() {
        userLoginListener.onLoggedOut()
        verify { editTypeStatisticsDao.clear() }
        verify { countryStatisticsDao.clear() }
        verify { currentWeekCountryStatisticsDao.clear() }
        verify { currentWeekEditTypeStatisticsDao.clear() }
        verify { activeDatesDao.clear() }
        verify { prefs.clearUserStatistics() }
        verify { listener.onCleared() }
    }

    @Test fun `update all`() {
        every { prefs.statisticsSynchronizedOnce } returns false
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
        verify {
            editTypeStatisticsDao.replaceAll(mapOf(
                "TestQuestTypeA" to 123,
                "TestQuestTypeB" to 44
            ))
        }
        verify {
            countryStatisticsDao.replaceAll(listOf(
                CountryStatistics("DE", 12, 5),
                CountryStatistics("US", 43, null),
            ))
        }
        verify {
            currentWeekEditTypeStatisticsDao.replaceAll(mapOf(
                "TestQuestTypeA" to 321,
                "TestQuestTypeB" to 33
            ))
        }
        verify {
            currentWeekCountryStatisticsDao.replaceAll(listOf(
                CountryStatistics("AT", 999, 88),
                CountryStatistics("IT", 99, null),
            ))
        }
        verify {
            activeDatesDao.replaceAll(listOf(
                LocalDate.parse("1999-04-08"),
                LocalDate.parse("1888-01-02")
            ))
        }
        verify { prefs.userActiveDatesRange = 12 }
        verify { prefs.userDaysActive = 333 }
        verify { prefs.isSynchronizingStatistics = false }
        verify { prefs.userGlobalRank = 999 }
        verify { prefs.userGlobalRankCurrentWeek = 111 }
        verify { prefs.userLastTimestampActive = 9999999 }
        verify { prefs.statisticsSynchronizedOnce = true }
        verify { listener.onUpdatedAll(true) }
    }
}
