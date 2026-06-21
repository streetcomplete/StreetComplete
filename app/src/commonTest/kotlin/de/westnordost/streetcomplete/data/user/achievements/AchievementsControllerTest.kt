package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.achievements.AchievementCondition.DaysActive
import de.westnordost.streetcomplete.data.user.achievements.AchievementCondition.EditsOfTypeCount
import de.westnordost.streetcomplete.data.user.achievements.AchievementCondition.TotalEditCount
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.resources.*
import dev.mokkery.answering.calls
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AchievementsControllerTest {

    private lateinit var userAchievementsDao: UserAchievementsDao
    private lateinit var userLinksDao: UserLinksDao
    private lateinit var statisticsSource: StatisticsSource
    private lateinit var allEditTypes: AllEditTypes
    private var allLinks: List<Link> = listOf()
    private var allAchievements: List<Achievement> = listOf()

    private lateinit var statisticsListener: StatisticsSource.Listener
    private lateinit var listener: AchievementsSource.Listener

    @BeforeTest fun setUp() {
        userAchievementsDao = mock() {
            every { getAll() } returns mapOf()
        }
        userLinksDao = mock() {
            every { getAll() } returns listOf()
        }
        statisticsSource = mock() {
            every { addListener(any()) } calls { (listener: StatisticsSource.Listener) ->
                statisticsListener = listener
            }
        }
        allEditTypes = AllEditTypes(listOf(
            QuestTypeRegistry(listOf(0 to QuestOne, 1 to QuestTwo)),
            OverlayRegistry(listOf(0 to OverlayOne)),
        ))

        listener = mock()

        allLinks = listOf()
        allAchievements = listOf()
    }

    private fun createAchievementsController(): AchievementsController =
        AchievementsController(
            statisticsSource, userAchievementsDao, userLinksDao,
            allEditTypes, allAchievements, allLinks
        ).also { it.addListener(listener) }

    @Test fun `unlocks DaysActive achievement`() {
        every { statisticsSource.daysActive } returns 1
        val achievement = achievement("daysActive", DaysActive)
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verify { userAchievementsDao.put("daysActive", 1) }
        verify { listener.onAchievementUnlocked(achievement, 1, emptyList()) }
    }

    @Test fun `unlocks TotalEditCount achievement`() {
        every { statisticsSource.getEditCount() } sequentially {
            returns(1)
            returns(2)
        }
        val achievement = achievement("allQuests", TotalEditCount)
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onAddedOne("QuestOne")
        verify { userAchievementsDao.put("allQuests", 1) }
        verify { listener.onAchievementUnlocked(achievement, 1, emptyList()) }

        statisticsListener.onAddedOne("OverlayOne")
        verify { userAchievementsDao.put("allQuests", 2) }
        verify { listener.onAchievementUnlocked(achievement, 2, emptyList()) }
    }

    @Test fun `unlocks EditsOfTypeCount achievement`() {
        every { statisticsSource.getEditCount(listOf("QuestOne", "QuestTwo", "OverlayOne")) } sequentially {
            returns(1)
            returns(2)
        }
        val achievement = achievement("veg", EditsOfTypeCount)
        allAchievements = listOf(achievement)

        createAchievementsController()

        statisticsListener.onAddedOne("QuestTwo")
        verify { userAchievementsDao.put("veg", 1) }
        verify { listener.onAchievementUnlocked(achievement, 1, emptyList()) }

        statisticsListener.onAddedOne("OverlayOne")
        verify { userAchievementsDao.put("veg", 2) }
        verify { listener.onAchievementUnlocked(achievement, 2, emptyList()) }
    }

    @Test fun `unlocks multiple locked levels of an achievement and grants those links`() {
        every { userAchievementsDao.getAll() } returns mapOf("allQuests" to 2)
        // already granted: link "a" and "c"
        every { userLinksDao.getAll() } returns listOf("a", "c")
        every { statisticsSource.getEditCount() } returns 5
        val achievement = achievement(
            id = "allQuests",
            condition = TotalEditCount,
            unlockedLinks = mapOf(
                1 to links("a", "b"),
                2 to links("c"),
                3 to links("d"), // 3 has one link
                // 4 has no links
                5 to links("e", "f") // 5 has two links
            )
        )
        allLinks = links("a", "b", "c", "d", "e", "f")
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onAddedOne("QuestOne")

        verify { userAchievementsDao.put("allQuests", 5) }
        verify { userLinksDao.addAll(listOf("b", "d", "e", "f")) }
        verify {
            listener.onAchievementUnlocked(
                achievement = achievement,
                level = 5,
                unlockedLinks = links("b", "d", "e", "f")
            )
        }
    }

    @Test fun `unlocks links not yet unlocked`() {
        every { statisticsSource.getEditCount() } returns 2
        allLinks = links("a", "b", "c", "d")
        allAchievements = listOf(achievement(
            id = "allQuests",
            condition = TotalEditCount,
            unlockedLinks = mapOf(
                1 to links("a", "b"),
                2 to links("c"),
                3 to links("d") // this shouldn't be unlocked
            )
        ))

        createAchievementsController()
        statisticsListener.onUpdatedAll(true)

        verify { userAchievementsDao.putAll(listOf("allQuests" to 2)) }
        verify { userLinksDao.addAll(listOf("a", "b", "c")) }
    }

    @Test fun `no achievement level above maxLevel will be granted`() {
        every { statisticsSource.daysActive } returns 100
        val achievement = achievement(
            id = "daysActive",
            condition = DaysActive,
            maxLevel = 5
        )
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verify { userAchievementsDao.put("daysActive", 5) }
        verify { listener.onAchievementUnlocked(achievement, 5, emptyList()) }
    }

    @Test fun `only updates achievements for given questType`() {
        // all achievements below should usually be granted
        every { statisticsSource.daysActive } returns 1
        every { statisticsSource.getEditCount(any<List<String>>()) } returns 1
        every { statisticsSource.getEditCount() } returns 1

        allAchievements = listOf(
            achievement("daysActive", DaysActive),
            achievement("blind", EditsOfTypeCount),
            achievement("car", EditsOfTypeCount),
            achievement("veg", EditsOfTypeCount),
            achievement("allQuests", TotalEditCount)
        )

        createAchievementsController()
        statisticsListener.onAddedOne("QuestOne")

        verify { userAchievementsDao.getAll() }
        verify { userAchievementsDao.put("car", 1) }
        verify { userAchievementsDao.put("veg", 1) }
        verify { userAchievementsDao.put("allQuests", 1) }
        verifyNoMoreCalls(userAchievementsDao)
    }

    @Test fun `only updates daysActive achievements`() {
        every { statisticsSource.daysActive } returns 1
        every { statisticsSource.getEditCount(any<List<String>>()) } returns 1
        every { statisticsSource.getEditCount() } returns 1

        allAchievements = listOf(
            achievement("daysActive", DaysActive),
            achievement("daysActive2", DaysActive),
            achievement("mixedAchievement", EditsOfTypeCount),
            achievement("allQuests", TotalEditCount)
        )

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verify { userAchievementsDao.getAll() }
        verify { userAchievementsDao.put("daysActive", 1) }
        verify { userAchievementsDao.put("daysActive2", 1) }
        verifyNoMoreCalls(userAchievementsDao)
    }

    @Test fun `clears all achievements on clearing statistics`() {
        createAchievementsController()
        statisticsListener.onCleared()

        verify { userAchievementsDao.clear() }
        verify { userLinksDao.clear() }
        verify { listener.onAllAchievementsUpdated() }
    }

    @Test fun `get all unlocked links`() {
        allLinks = links("a", "b", "c")
        every { userLinksDao.getAll() } returns listOf("a", "b")
        assertEquals(
            links("a", "b"),
            createAchievementsController().getLinks()
        )
    }

    @Test fun `get all unlocked achievements`() {
        allAchievements = listOf(
            achievement("daysActive", DaysActive),
            achievement("otherAchievement", EditsOfTypeCount)
        )
        every { userAchievementsDao.getAll() } returns mapOf("daysActive" to 3)
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
    Achievement(id, null, Res.string.action_settings, null, condition, func, unlockedLinks, maxLevel)

private fun links(vararg ids: String): List<Link> =
    ids.map { id -> Link(id, "url", "title", LinkCategory.INTRO, null, null) }

private object QuestOne : QuestType {
    override val icon = 0
    override val title = Res.string.quest_address_title
    override val wikiLink: String? = null
    override val achievements = listOf(EditTypeAchievement.CAR, EditTypeAchievement.VEG)
}

private object QuestTwo : QuestType {
    override val icon = 0
    override val title = Res.string.quest_address_title
    override val wikiLink: String? = null
    override val achievements = listOf(EditTypeAchievement.BLIND, EditTypeAchievement.VEG)
}

private object OverlayOne : Overlay {
    override val icon = 0
    override val title = Res.string.quest_address_title
    override val wikiLink: String? = null
    override val changesetComment = ""
    override fun getStyledElements(mapData: MapDataWithGeometry) = emptySequence<Pair<Element, OverlayStyle>>()

    override val achievements = listOf(EditTypeAchievement.BLIND, EditTypeAchievement.VEG)
}
