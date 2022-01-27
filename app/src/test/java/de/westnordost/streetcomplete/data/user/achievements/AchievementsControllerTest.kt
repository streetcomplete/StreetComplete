package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

class AchievementsControllerTest {

    private lateinit var userAchievementsDao: UserAchievementsDao
    private lateinit var userLinksDao: UserLinksDao
    private lateinit var statisticsSource: StatisticsSource
    private var allLinks: List<Link> = listOf()
    private var allAchievements: List<Achievement> = listOf()
    private lateinit var questTypeRegistry: QuestTypeRegistry

    private lateinit var statisticsListener: StatisticsSource.Listener
    private lateinit var listener: AchievementsSource.Listener

    @Before fun setUp() {
        userAchievementsDao = mock()
        on(userAchievementsDao.getAll()).thenReturn(mapOf())
        userLinksDao = mock()
        statisticsSource = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(QuestOne, QuestTwo))

        listener = mock()

        allLinks = listOf()
        allAchievements = listOf()

        on(statisticsSource.addListener(any())).then { invocation ->
            statisticsListener = invocation.getArgument(0)
            Unit
        }
    }

    private fun createAchievementsController(): AchievementsController =
        AchievementsController(
            statisticsSource, userAchievementsDao, userLinksDao,
            questTypeRegistry, allAchievements, allLinks
        ).also { it.addListener(listener) }

    @Test fun `unlocks DaysActive achievement`() {
        on(statisticsSource.daysActive).thenReturn(1)
        val achievement = achievement("daysActive", DaysActive)
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verify(userAchievementsDao).put("daysActive", 1)
        verify(listener).onAchievementUnlocked(achievement, 1)
    }

    @Test fun `unlocks TotalSolvedQuests achievement`() {
        on(statisticsSource.getSolvedCount()).thenReturn(1)
        val achievement = achievement("allQuests", TotalSolvedQuests)
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onAddedOne(QuestOne)

        verify(userAchievementsDao).put("allQuests", 1)
        verify(listener).onAchievementUnlocked(achievement, 1)
    }

    @Test fun `unlocks QuestType achievement`() {
        on(statisticsSource.getSolvedCount(listOf(QuestOne, QuestTwo))).thenReturn(1)
        val achievement = achievement("mixedAchievement", SolvedQuestsOfTypes)
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onAddedOne(QuestTwo)

        verify(userAchievementsDao).put("mixedAchievement", 1)
        verify(listener).onAchievementUnlocked(achievement, 1)
    }

    @Test fun `unlocks multiple locked levels of an achievement and grants those links`() {
        on(userAchievementsDao.getAll()).thenReturn(mapOf("allQuests" to 2))
        on(statisticsSource.getSolvedCount()).thenReturn(5)
        val achievement = achievement(
            id = "allQuests",
            condition = TotalSolvedQuests,
            unlockedLinks = mapOf(
                1 to links("a", "b"),
                2 to links("c"),
                3 to links("d"), // 3 has one link
                // 4 has no links
                5 to links("e", "f") // 5 has two links
            )
        )
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onAddedOne(QuestOne)

        verify(userAchievementsDao).put("allQuests", 5)
        verify(userLinksDao).addAll(listOf("d", "e", "f"))
        verify(listener).onAchievementUnlocked(achievement, 3)
        verify(listener).onAchievementUnlocked(achievement, 4)
        verify(listener).onAchievementUnlocked(achievement, 5)
    }

    @Test fun `unlocks links not yet unlocked`() {
        on(userAchievementsDao.getAll()).thenReturn(mapOf("allQuests" to 2))
        allAchievements = listOf(achievement(
            id = "allQuests",
            condition = TotalSolvedQuests,
            unlockedLinks = mapOf(
                1 to links("a", "b"),
                2 to links("c"),
                3 to links("d") // this shouldn't be unlocked
            )
        ))

        createAchievementsController()
        statisticsListener.onUpdatedAll()

        verify(userLinksDao).addAll(listOf("a", "b", "c"))
    }

    @Test fun `no achievement level above maxLevel will be granted`() {
        on(statisticsSource.daysActive).thenReturn(100)
        val achievement = achievement(
            id = "daysActive",
            condition = DaysActive,
            maxLevel = 5
        )
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verify(userAchievementsDao).put("daysActive", 5)
        verify(listener).onAchievementUnlocked(achievement, 5)
    }

    @Test fun `only updates achievements for given questType`() {
        // all achievements below should usually be granted
        on(statisticsSource.daysActive).thenReturn(1)
        on(statisticsSource.getSolvedCount(any<List<QuestType<*>>>())).thenReturn(1)
        on(statisticsSource.getSolvedCount()).thenReturn(1)

        allAchievements = listOf(
            achievement("daysActive", DaysActive),
            achievement("otherAchievement", SolvedQuestsOfTypes),
            achievement("thisAchievement", SolvedQuestsOfTypes),
            achievement("mixedAchievement", SolvedQuestsOfTypes),
            achievement("allQuests", TotalSolvedQuests)
        )

        createAchievementsController()
        statisticsListener.onAddedOne(QuestOne)

        verify(userAchievementsDao).getAll()
        verify(userAchievementsDao).put("thisAchievement", 1)
        verify(userAchievementsDao).put("mixedAchievement", 1)
        verify(userAchievementsDao).put("allQuests", 1)
        verifyNoMoreInteractions(userAchievementsDao)
    }

    @Test fun `only updates daysActive achievements`() {
        on(statisticsSource.daysActive).thenReturn(1)
        on(statisticsSource.getSolvedCount(any<List<QuestType<*>>>())).thenReturn(1)
        on(statisticsSource.getSolvedCount()).thenReturn(1)

        allAchievements = listOf(
            achievement("daysActive", DaysActive),
            achievement("daysActive2", DaysActive),
            achievement("mixedAchievement", SolvedQuestsOfTypes),
            achievement("allQuests", TotalSolvedQuests)
        )

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verify(userAchievementsDao).getAll()
        verify(userAchievementsDao).put("daysActive", 1)
        verify(userAchievementsDao).put("daysActive2", 1)
        verifyNoMoreInteractions(userAchievementsDao)
    }

    @Test fun `clears all achievements on clearing statistics`() {
        createAchievementsController()
        statisticsListener.onCleared()

        verify(userAchievementsDao).clear()
        verify(userLinksDao).clear()
        verify(listener).onAllAchievementsUpdated()
    }

    @Test fun `get all unlocked links`() {
        allLinks = links("a", "b", "c")
        on(userLinksDao.getAll()).thenReturn(listOf("a", "b"))
        assertEquals(
            links("a", "b"),
            createAchievementsController().getLinks()
        )
    }

    @Test fun `get all unlocked achievements`() {
        allAchievements = listOf(
            achievement("daysActive", DaysActive),
            achievement("otherAchievement", SolvedQuestsOfTypes)
        )
        on(userAchievementsDao.getAll()).thenReturn(mapOf(
            "daysActive" to 3
        ))
        assertEquals(
            listOf(achievement("daysActive", DaysActive) to 3),
            createAchievementsController().getAchievements()
        )
    }
}

private fun achievement(
    id: String,
    condition: AchievementCondition,
    func: (Int) -> Int = { 1 },
    unlockedLinks: Map<Int, List<Link>> = emptyMap(),
    maxLevel: Int = -1
): Achievement =
    Achievement(id, 0, 0, 0, condition, func, unlockedLinks, maxLevel)

private fun links(vararg ids: String): List<Link> =
    ids.map { id -> Link(id, "url", "title", LinkCategory.INTRO, null, null) }

private fun questTypeAchievements(achievementIds: List<String>): List<QuestTypeAchievement> =
    achievementIds.map {
        val questTypeAchievement: QuestTypeAchievement = mock()
        on(questTypeAchievement.id).thenReturn(it)
        questTypeAchievement
    }

private object QuestOne : QuestType<Int> {
    override val icon = 0
    override val title = 0
    override fun createForm(): AbstractQuestAnswerFragment<Int> = mock()
    override val questTypeAchievements =
        questTypeAchievements(listOf("thisAchievement", "mixedAchievement"))
}

private object QuestTwo : QuestType<Int> {
    override val icon = 0
    override val title = 0
    override fun createForm(): AbstractQuestAnswerFragment<Int> = mock()
    override val questTypeAchievements =
        questTypeAchievements(listOf("otherAchievement", "mixedAchievement"))
}
