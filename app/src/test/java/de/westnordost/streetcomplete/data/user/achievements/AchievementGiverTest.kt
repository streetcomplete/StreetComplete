package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.notifications.NewUserAchievementsDao
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import de.westnordost.streetcomplete.data.user.UserStore
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

class AchievementGiverTest {

    private lateinit var userAchievementsDao: UserAchievementsDao
    private lateinit var newUserAchievementsDao: NewUserAchievementsDao
    private lateinit var userLinksDao: UserLinksDao
    private lateinit var questStatisticsDao: QuestStatisticsDao
    private lateinit var userStore: UserStore
    private var allAchievements: MutableList<Achievement> = mutableListOf()
    private lateinit var achievementGiver: AchievementGiver

    @Before fun setUp() {
        userAchievementsDao = mock()
        on(userAchievementsDao.getAll()).thenReturn(mapOf())
        newUserAchievementsDao = mock()
        userLinksDao = mock()
        questStatisticsDao = mock()
        userStore = mock()

        allAchievements.clear()

        achievementGiver = AchievementGiver(userAchievementsDao, newUserAchievementsDao,
            userLinksDao, questStatisticsDao, allAchievements, userStore)
    }


    @Test fun `unlocks DaysActive achievement`() {
        on(userStore.daysActive).thenReturn(1)
        allAchievements.addAll(listOf(achievement("daysActive", DaysActive)))

        achievementGiver.updateAllAchievements()

        verify(userAchievementsDao).put("daysActive", 1)
        verify(newUserAchievementsDao).push("daysActive" to 1)
    }

    @Test fun `unlocks TotalSolvedQuests achievement`() {
        on(questStatisticsDao.getTotalAmount()).thenReturn(1)
        allAchievements.addAll(listOf(achievement("allQuests", TotalSolvedQuests)))

        achievementGiver.updateAllAchievements()
        verify(userAchievementsDao).put("allQuests", 1)
        verify(newUserAchievementsDao).push("allQuests" to 1)
    }

    @Test fun `unlocks QuestType achievement`() {
        on(questStatisticsDao.getAmount(listOf("a","b"))).thenReturn(1)

        allAchievements.addAll(listOf(achievement("questsAbc", SolvedQuestsOfTypes(listOf("a","b")))))

        achievementGiver.updateAllAchievements()

        verify(userAchievementsDao).put("questsAbc", 1)
        verify(newUserAchievementsDao).push("questsAbc" to 1)
    }

    @Test fun `unlocks multiple locked levels of an achievement and grants those links`() {
        on(userAchievementsDao.getAll()).thenReturn(mapOf("allQuests" to 2))
        on(questStatisticsDao.getTotalAmount()).thenReturn(5)
        allAchievements.addAll(listOf(achievement(
            id = "allQuests",
            condition = TotalSolvedQuests,
            unlockedLinks = mapOf(
                1 to links("a","b"),
                2 to links("c"),
                3 to links("d"), // 3 has one link
                // 4 has no links
                5 to links("e","f") // 5 has two links
            ))))

        achievementGiver.updateAllAchievements()

        verify(userAchievementsDao).put("allQuests", 5)
        verify(newUserAchievementsDao).push("allQuests" to 3)
        verify(newUserAchievementsDao).push("allQuests" to 4)
        verify(newUserAchievementsDao).push("allQuests" to 5)
        verify(userLinksDao).addAll(listOf("d","e","f"))
    }

    @Test fun `updateAchievementLinks unlocks links not yet unlocked`() {
        on(userAchievementsDao.getAll()).thenReturn(mapOf("allQuests" to 2))
        allAchievements.addAll(listOf(achievement(
            id = "allQuests",
            condition = TotalSolvedQuests,
            unlockedLinks = mapOf(
                1 to links("a","b"),
                2 to links("c"),
                3 to links("d") // this shouldn't be unlocked
            ))))

        achievementGiver.updateAchievementLinks()

        verify(userLinksDao).addAll(listOf("a","b","c"))
    }



    @Test fun `no achievement level above maxLevel will be granted`() {
        on(userStore.daysActive).thenReturn(100)
        allAchievements.addAll(listOf(achievement(
            id = "daysActive",
            condition = DaysActive,
            maxLevel = 5
        )))

        achievementGiver.updateAllAchievements()

        verify(userAchievementsDao).put("daysActive", 5)
        verify(newUserAchievementsDao).push("daysActive" to 1)
        verify(newUserAchievementsDao).push("daysActive" to 2)
        verify(newUserAchievementsDao).push("daysActive" to 3)
        verify(newUserAchievementsDao).push("daysActive" to 4)
        verify(newUserAchievementsDao).push("daysActive" to 5)
    }

    @Test fun `updateQuestTypeAchievements only updates achievements for given questType`() {
        // all achievements below should usually be granted
        on(userStore.daysActive).thenReturn(1)
        on(questStatisticsDao.getAmount(any<List<String>>())).thenReturn(1)
        on(questStatisticsDao.getTotalAmount()).thenReturn(1)

        allAchievements.addAll(listOf(
            achievement("daysActive", DaysActive),
            achievement("otherAchievement", SolvedQuestsOfTypes(listOf("OtherQuest"))),
            achievement("thisAchievement", SolvedQuestsOfTypes(listOf("ThisQuest"))),
            achievement("mixedAchievement", SolvedQuestsOfTypes(listOf("ThisQuest", "OtherQuest"))),
            achievement("allQuests", TotalSolvedQuests)
        ))

        achievementGiver.updateQuestTypeAchievements("ThisQuest")

        verify(userAchievementsDao).getAll()
        verify(userAchievementsDao).put("thisAchievement", 1)
        verify(userAchievementsDao).put("mixedAchievement", 1)
        verify(userAchievementsDao).put("allQuests", 1)
        verifyNoMoreInteractions(userAchievementsDao)
    }

    @Test fun `updateDaysActiveAchievements only updates daysActive achievements`() {
        on(userStore.daysActive).thenReturn(1)
        on(questStatisticsDao.getAmount(any<List<String>>())).thenReturn(1)
        on(questStatisticsDao.getTotalAmount()).thenReturn(1)

        allAchievements.addAll(listOf(
            achievement("daysActive", DaysActive),
            achievement("daysActive2", DaysActive),
            achievement("otherAchievement", SolvedQuestsOfTypes(listOf("ThisQuest", "OtherQuest"))),
            achievement("allQuests", TotalSolvedQuests)
        ))

        achievementGiver.updateDaysActiveAchievements()

        verify(userAchievementsDao).getAll()
        verify(userAchievementsDao).put("daysActive", 1)
        verify(userAchievementsDao).put("daysActive2", 1)
        verifyNoMoreInteractions(userAchievementsDao)
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
}