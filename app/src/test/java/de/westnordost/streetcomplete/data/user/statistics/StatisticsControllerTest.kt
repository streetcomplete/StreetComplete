package de.westnordost.streetcomplete.data.user.statistics

import android.content.SharedPreferences
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.p
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.verify
import java.util.concurrent.FutureTask

class StatisticsControllerTest {

    private lateinit var questTypeStatisticsDao: QuestTypeStatisticsDao
    private lateinit var countryStatisticsDao: CountryStatisticsDao
    private lateinit var countryBoundaries: CountryBoundaries
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var prefs: SharedPreferences
    private lateinit var loginStatusSource: UserLoginStatusSource
    private lateinit var loginStatusListener: UserLoginStatusSource.Listener

    private lateinit var statisticsController: StatisticsController
    private lateinit var listener: StatisticsSource.Listener

    private val questA = TestQuestTypeA()
    private val questB = TestQuestTypeB()

    @Before fun setUp() {
        questTypeStatisticsDao = mock()
        countryStatisticsDao = mock()
        countryBoundaries = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(questA, questB))
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
            questTypeStatisticsDao, countryStatisticsDao, ft, questTypeRegistry,
            prefs, loginStatusSource
        )
        statisticsController.addListener(listener)
    }

    @Test fun getSolvedCount() {
        on(questTypeStatisticsDao.getTotalAmount()).thenReturn(5)
        assertEquals(
            5,
            statisticsController.getSolvedCount()
        )
    }

    @Test fun `adding one`() {
        on(countryBoundaries.getIds(anyDouble(), anyDouble())).thenReturn(listOf("US-TX", "US", "World"))
        statisticsController.addOne(questA, p(0.0, 0.0))

        verify(questTypeStatisticsDao).addOne("TestQuestTypeA")
        verify(countryStatisticsDao).addOne("US")
        verify(listener).onAddedOne(questA)
    }

    @Test fun `adding one one day later`() {
        on(prefs.getInt(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 0)).thenReturn(0)

        statisticsController.addOne(questA, p(0.0, 0.0))

        verify(questTypeStatisticsDao).addOne("TestQuestTypeA")
        verify(listener).onAddedOne(questA)
        verify(listener).onUpdatedDaysActive()
    }

    @Test fun `subtracting one`() {
        on(countryBoundaries.getIds(anyDouble(), anyDouble())).thenReturn(listOf("US-TX", "US", "World"))
        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verify(questTypeStatisticsDao).subtractOne("TestQuestTypeA")
        verify(countryStatisticsDao).subtractOne("US")
        verify(listener).onSubtractedOne(questA)
    }

    @Test fun `subtracting one one day later`() {
        on(prefs.getInt(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 0)).thenReturn(0)

        statisticsController.subtractOne(questA, p(0.0, 0.0))

        verify(questTypeStatisticsDao).subtractOne("TestQuestTypeA")
        verify(listener).onSubtractedOne(questA)
        verify(listener).onUpdatedDaysActive()
    }

    @Test fun `clear all`() {
        val editor: SharedPreferences.Editor = mock()
        on(prefs.edit()).thenReturn(editor)

        loginStatusListener.onLoggedOut()

        verify(questTypeStatisticsDao).clear()
        verify(countryStatisticsDao).clear()
        verify(editor).remove(Prefs.USER_DAYS_ACTIVE)
        verify(editor).remove(Prefs.IS_SYNCHRONIZING_STATISTICS)
        verify(editor).remove(Prefs.USER_GLOBAL_RANK)
        verify(editor).remove(Prefs.USER_LAST_TIMESTAMP_ACTIVE)
        verify(listener).onCleared()
    }

    @Test fun `update all`() {
        val editor: SharedPreferences.Editor = mock()
        on(prefs.edit()).thenReturn(editor)

        statisticsController.updateAll(Statistics(
            questTypes = listOf(
                QuestTypeStatistics(questA, 123),
                QuestTypeStatistics(questB, 44),
            ),
            countries = listOf(
                CountryStatistics("DE", 12, 5),
                CountryStatistics("US", 43, null),
            ),
            rank = 999,
            daysActive = 333,
            lastUpdate = 9999999,
            isAnalyzing = false
        ))
        verify(questTypeStatisticsDao).replaceAll(mapOf(
            "TestQuestTypeA" to 123,
            "TestQuestTypeB" to 44
        ))
        verify(countryStatisticsDao).replaceAll(listOf(
            CountryStatistics("DE", 12, 5),
            CountryStatistics("US", 43, null),
        ))
        verify(editor).putInt(Prefs.USER_DAYS_ACTIVE, 333)
        verify(editor).putBoolean(Prefs.IS_SYNCHRONIZING_STATISTICS, false)
        verify(editor).putInt(Prefs.USER_GLOBAL_RANK, 999)
        verify(editor).putLong(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 9999999)
        verify(listener).onUpdatedAll()
    }
}
