package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AchievementsControllerTest {

    @Mock private lateinit var userAchievementsDao: UserAchievementsDao
    @Mock private lateinit var userLinksDao: UserLinksDao
    @Mock private lateinit var statisticsSource: StatisticsSource
    private lateinit var allEditTypes: AllEditTypes
    private var allLinks: List<Link> = listOf()
    private var allAchievements: List<Achievement> = listOf()

    private lateinit var statisticsListener: StatisticsSource.Listener
    private lateinit var listener: AchievementsSource.Listener

    // dummy
    @Mock private lateinit var form: AbstractQuestForm
    @Mock private lateinit var editTypeAchievement: EditTypeAchievement

    @BeforeTest fun setUp() {
        form = mock(classOf<AbstractQuestForm>())
        userAchievementsDao = mock(classOf<UserAchievementsDao>())
        every { userAchievementsDao.getAll() }.returns(mapOf())
        userLinksDao = mock(classOf<UserLinksDao>())
        statisticsSource = mock(classOf<StatisticsSource>())
        allEditTypes = AllEditTypes(listOf(
            QuestTypeRegistry(listOf(0 to QuestOne, 1 to QuestTwo)),
            OverlayRegistry(listOf(0 to OverlayOne)),
        ))

        listener = mock(classOf<AchievementsSource.Listener>())

        allLinks = listOf()
        allAchievements = listOf()

        every { statisticsSource.addListener(any()) }.invokes { arguments ->
            statisticsListener = arguments as StatisticsSource.Listener
            Unit
        }
    }

    private fun createAchievementsController(): AchievementsController =
        AchievementsController(
            statisticsSource, userAchievementsDao, userLinksDao,
            allEditTypes, allAchievements, allLinks
        ).also { it.addListener(listener) }

    @Test fun `unlocks DaysActive achievement`() {
        every { statisticsSource.daysActive }.returns(1)
        val achievement = achievement("daysActive", DaysActive)
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verifyInvokedExactlyOnce { userAchievementsDao.put("daysActive", 1) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 1) }
    }

    @Test fun `unlocks TotalEditCount achievement`() {
        every { statisticsSource.getEditCount() }.returnsMany(1, 2)
        val achievement = achievement("allQuests", TotalEditCount)
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onAddedOne("QuestOne")
        verifyInvokedExactlyOnce { userAchievementsDao.put("allQuests", 1) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 1) }

        statisticsListener.onAddedOne("OverlayOne")
        verifyInvokedExactlyOnce { userAchievementsDao.put("allQuests", 2) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 2) }
    }

    @Test fun `unlocks EditsOfTypeCount achievement`() {
        every { statisticsSource.getEditCount(listOf("QuestOne", "QuestTwo", "OverlayOne")) }.returnsMany(1, 2)
        val achievement = achievement("mixedAchievement", EditsOfTypeCount)
        allAchievements = listOf(achievement)

        createAchievementsController()

        statisticsListener.onAddedOne("QuestTwo")
        verifyInvokedExactlyOnce { userAchievementsDao.put("mixedAchievement", 1) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 1) }

        statisticsListener.onAddedOne("OverlayOne")
        verifyInvokedExactlyOnce { userAchievementsDao.put("mixedAchievement", 2) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 2) }
    }

    @Test fun `unlocks multiple locked levels of an achievement and grants those links`() {
        every { userAchievementsDao.getAll() }.returns(mapOf("allQuests" to 2))
        every { statisticsSource.getEditCount() }.returns(5)
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
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onAddedOne("QuestOne")

        verifyInvokedExactlyOnce { userAchievementsDao.put("allQuests", 5) }
        verifyInvokedExactlyOnce { userLinksDao.addAll(listOf("d", "e", "f")) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 3) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 4) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 5) }
    }

    @Test fun `unlocks links not yet unlocked`() {
        every { userAchievementsDao.getAll() }.returns(mapOf("allQuests" to 2))
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
        statisticsListener.onUpdatedAll()

        verifyInvokedExactlyOnce { userLinksDao.addAll(listOf("a", "b", "c")) }
    }

    @Test fun `no achievement level above maxLevel will be granted`() {
        every { statisticsSource.daysActive }.returns(100)
        val achievement = achievement(
            id = "daysActive",
            condition = DaysActive,
            maxLevel = 5
        )
        allAchievements = listOf(achievement)

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verifyInvokedExactlyOnce { userAchievementsDao.put("daysActive", 5) }
        verifyInvokedExactlyOnce { listener.onAchievementUnlocked(achievement, 5) }
    }

    @Test fun `only updates achievements for given questType`() {
        // all achievements below should usually be granted
        every { statisticsSource.daysActive }.returns(1)
        every { statisticsSource.getEditCount(any<List<String>>()) }.returns(1)
        every { statisticsSource.getEditCount() }.returns(1)

        allAchievements = listOf(
            achievement("daysActive", DaysActive),
            achievement("otherAchievement", EditsOfTypeCount),
            achievement("thisAchievement", EditsOfTypeCount),
            achievement("mixedAchievement", EditsOfTypeCount),
            achievement("allQuests", TotalEditCount)
        )

        createAchievementsController()
        statisticsListener.onAddedOne("QuestOne")

        verifyInvokedExactlyOnce { userAchievementsDao.getAll() }
        verifyInvokedExactlyOnce { userAchievementsDao.put("thisAchievement", 1) }
        verifyInvokedExactlyOnce { userAchievementsDao.put("mixedAchievement", 1) }
        verifyInvokedExactlyOnce { userAchievementsDao.put("allQuests", 1) }
        // verifyNoMoreInteractions(userAchievementsDao)
    }

    @Test fun `only updates daysActive achievements`() {
        every { statisticsSource.daysActive }.returns(1)
        every { statisticsSource.getEditCount(any<List<String>>()) }.returns(1)
        every { statisticsSource.getEditCount() }.returns(1)

        allAchievements = listOf(
            achievement("daysActive", DaysActive),
            achievement("daysActive2", DaysActive),
            achievement("mixedAchievement", EditsOfTypeCount),
            achievement("allQuests", TotalEditCount)
        )

        createAchievementsController()
        statisticsListener.onUpdatedDaysActive()

        verifyInvokedExactlyOnce { userAchievementsDao.getAll() }
        verifyInvokedExactlyOnce { userAchievementsDao.put("daysActive", 1) }
        verifyInvokedExactlyOnce { userAchievementsDao.put("daysActive2", 1) }
        // verifyNoMoreInteractions(userAchievementsDao)
    }

    @Test fun `clears all achievements on clearing statistics`() {
        createAchievementsController()
        statisticsListener.onCleared()

        verifyInvokedExactlyOnce { userAchievementsDao.clear() }
        verifyInvokedExactlyOnce { userLinksDao.clear() }
        verifyInvokedExactlyOnce { listener.onAllAchievementsUpdated() }
    }

    @Test fun `get all unlocked links`() {
        allLinks = links("a", "b", "c")
        every { userLinksDao.getAll() }.returns(listOf("a", "b"))
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
        every { userAchievementsDao.getAll() }.returns(mapOf(
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

private fun editTypeAchievements(achievementIds: List<String>): List<EditTypeAchievement> =
    achievementIds.map {
        // todo can't mock enums
        val editTypeAchievement: EditTypeAchievement = mock(classOf<EditTypeAchievement>())
        every { editTypeAchievement.id }.returns(it)
        editTypeAchievement
    }

private object QuestOne : QuestType {
    override val icon = 0
    override val title = 0
    override val wikiLink: String? = null
    override fun createForm(): AbstractQuestForm = mock(classOf<AbstractQuestForm>())
    override val achievements = editTypeAchievements(listOf("thisAchievement", "mixedAchievement"))
}

private object QuestTwo : QuestType {
    override val icon = 0
    override val title = 0
    override val wikiLink: String? = null
    override fun createForm(): AbstractQuestForm = mock(classOf<AbstractQuestForm>())
    override val achievements = editTypeAchievements(listOf("otherAchievement", "mixedAchievement"))
}

private object OverlayOne : Overlay {
    override val icon = 0
    override val title = 0
    override val wikiLink: String? = null
    override val changesetComment = ""
    override fun getStyledElements(mapData: MapDataWithGeometry) = emptySequence<Pair<Element, Style>>()
    override fun createForm(element: Element?): AbstractOverlayForm? = null

    override val achievements = editTypeAchievements(listOf("otherAchievement", "mixedAchievement"))
}
