package de.westnordost.streetcomplete.data.user.statistics

import com.russhwolf.settings.ObservableSettings
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StatisticsControllerTest {

    @Mock private lateinit var editTypeStatisticsDao: EditTypeStatisticsDao
    @Mock private lateinit var countryStatisticsDao: CountryStatisticsDao
    @Mock private lateinit var currentWeekEditTypeStatisticsDao: EditTypeStatisticsDao
    @Mock private lateinit var currentWeekCountryStatisticsDao: CountryStatisticsDao
    @Mock private lateinit var activeDatesDao: ActiveDatesDao
    // todo: not mockable because it's from a library
    private lateinit var countryBoundaries: CountryBoundaries
    @Mock private lateinit var prefs: ObservableSettings
    @Mock private lateinit var loginStatusSource: UserLoginStatusSource
    @Mock  private lateinit var loginStatusListener: UserLoginStatusSource.Listener

    private lateinit var statisticsController: StatisticsController
    @Mock private lateinit var listener: StatisticsSource.Listener

    private val questA = "TestQuestTypeA"
    private val questB = "TestQuestTypeB"

    @BeforeTest fun setUp() {
        editTypeStatisticsDao = mock(classOf<EditTypeStatisticsDao>())
        countryStatisticsDao = mock(classOf<CountryStatisticsDao>())
        currentWeekEditTypeStatisticsDao = mock(classOf<EditTypeStatisticsDao>())
        currentWeekCountryStatisticsDao = mock(classOf<CountryStatisticsDao>())
        activeDatesDao = mock(classOf<ActiveDatesDao>())
        countryBoundaries = mock(classOf<CountryBoundaries>())
        prefs = mock(classOf<ObservableSettings>())
        listener = mock(classOf<StatisticsSource.Listener>())
        loginStatusSource = mock(classOf<UserLoginStatusSource>())

        every { loginStatusSource.addListener(any()) }.invokes { arguments ->
            loginStatusListener = arguments[0] as UserLoginStatusSource.Listener
            Unit
        }

        statisticsController = StatisticsController(
            editTypeStatisticsDao, countryStatisticsDao,
            currentWeekEditTypeStatisticsDao, currentWeekCountryStatisticsDao,
            activeDatesDao,
            lazyOf(countryBoundaries), prefs, loginStatusSource
        )
        statisticsController.addListener(listener)
    }

    @Test fun getSolvedCount() {
        every { editTypeStatisticsDao.getTotalAmount() }.returns(5)
        assertEquals(
            5,
            statisticsController.getEditCount()
        )
    }

    @Test fun `adding one`() {
        every { countryBoundaries.getIds(any(), any()) }.returns(listOf("US-TX", "US", "World"))
        statisticsController.addOne(questA, p(0.0, 0.0))

        verifyInvokedExactlyOnce { editTypeStatisticsDao.addOne("TestQuestTypeA") }
        verifyInvokedExactlyOnce { countryStatisticsDao.addOne("US") }
        verifyInvokedExactlyOnce { currentWeekEditTypeStatisticsDao.addOne("TestQuestTypeA") }
        verifyInvokedExactlyOnce { currentWeekCountryStatisticsDao.addOne("US") }
        verifyInvokedExactlyOnce { activeDatesDao.addToday() }
        verifyInvokedExactlyOnce { listener.onAddedOne(questA) }
    }

    @Test fun `adding one one day later`() {
        every { prefs.getInt(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 0) }.returns(0)

        statisticsController.addOne(questA, p(0.0, 0.0))

        verifyInvokedExactlyOnce { editTypeStatisticsDao.addOne("TestQuestTypeA") }
        verifyInvokedExactlyOnce { currentWeekEditTypeStatisticsDao.addOne("TestQuestTypeA") }
        verifyInvokedExactlyOnce { activeDatesDao.addToday() }
        verifyInvokedExactlyOnce { listener.onAddedOne(questA) }
        verifyInvokedExactlyOnce { listener.onUpdatedDaysActive() }
    }

    @Test fun `subtracting one`() {
        every { countryBoundaries.getIds(any(), any()) }.returns(listOf("US-TX", "US", "World"))
        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verifyInvokedExactlyOnce { editTypeStatisticsDao.subtractOne("TestQuestTypeA") }
        verifyInvokedExactlyOnce { countryStatisticsDao.subtractOne("US") }
        verifyInvokedExactlyOnce { currentWeekEditTypeStatisticsDao.subtractOne("TestQuestTypeA") }
        verifyInvokedExactlyOnce { currentWeekCountryStatisticsDao.subtractOne("US") }
        verifyInvokedExactlyOnce { activeDatesDao.addToday() }
        verifyInvokedExactlyOnce { listener.onSubtractedOne(questA) }
    }

    @Test fun `subtracting one one day later`() {
        every { prefs.getInt(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 0) }.returns(0)

        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verifyInvokedExactlyOnce { editTypeStatisticsDao.subtractOne("TestQuestTypeA") }
        verifyInvokedExactlyOnce { currentWeekEditTypeStatisticsDao.subtractOne("TestQuestTypeA") }
        verifyInvokedExactlyOnce { activeDatesDao.addToday() }
        verifyInvokedExactlyOnce { listener.onSubtractedOne(questA) }
        verifyInvokedExactlyOnce { listener.onUpdatedDaysActive() }
    }

    @Test fun `clear all`() {
        loginStatusListener.onLoggedOut()

        verifyInvokedExactlyOnce { editTypeStatisticsDao.clear() }
        verifyInvokedExactlyOnce { countryStatisticsDao.clear() }
        verifyInvokedExactlyOnce { currentWeekCountryStatisticsDao.clear() }
        verifyInvokedExactlyOnce { currentWeekEditTypeStatisticsDao.clear() }
        verifyInvokedExactlyOnce { activeDatesDao.clear() }
        verifyInvokedExactlyOnce { prefs.remove(Prefs.USER_DAYS_ACTIVE) }
        verifyInvokedExactlyOnce { prefs.remove(Prefs.IS_SYNCHRONIZING_STATISTICS) }
        verifyInvokedExactlyOnce { prefs.remove(Prefs.USER_GLOBAL_RANK) }
        verifyInvokedExactlyOnce { prefs.remove(Prefs.USER_GLOBAL_RANK_CURRENT_WEEK) }
        verifyInvokedExactlyOnce { prefs.remove(Prefs.USER_LAST_TIMESTAMP_ACTIVE) }
        verifyInvokedExactlyOnce { listener.onCleared() }
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
        verifyInvokedExactlyOnce { editTypeStatisticsDao.replaceAll(mapOf(
            "TestQuestTypeA" to 123,
            "TestQuestTypeB" to 44
        )) }
        verifyInvokedExactlyOnce { countryStatisticsDao.replaceAll(listOf(
            CountryStatistics("DE", 12, 5),
            CountryStatistics("US", 43, null),
        )) }
        verifyInvokedExactlyOnce { currentWeekEditTypeStatisticsDao.replaceAll(mapOf(
            "TestQuestTypeA" to 321,
            "TestQuestTypeB" to 33
        )) }
        verifyInvokedExactlyOnce { currentWeekCountryStatisticsDao.replaceAll(listOf(
            CountryStatistics("AT", 999, 88),
            CountryStatistics("IT", 99, null),
        )) }
        verifyInvokedExactlyOnce { activeDatesDao.replaceAll(listOf(
            LocalDate.parse("1999-04-08"),
            LocalDate.parse("1888-01-02")
        )) }
        verifyInvokedExactlyOnce { prefs.putInt(Prefs.ACTIVE_DATES_RANGE, 12) }
        verifyInvokedExactlyOnce { prefs.putInt(Prefs.USER_DAYS_ACTIVE, 333) }
        verifyInvokedExactlyOnce { prefs.putBoolean(Prefs.IS_SYNCHRONIZING_STATISTICS, false) }
        verifyInvokedExactlyOnce { prefs.putInt(Prefs.USER_GLOBAL_RANK, 999) }
        verifyInvokedExactlyOnce { prefs.putInt(Prefs.USER_GLOBAL_RANK_CURRENT_WEEK, 111) }
        verifyInvokedExactlyOnce { prefs.putLong(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 9999999) }
        verifyInvokedExactlyOnce { listener.onUpdatedAll() }
    }
}
