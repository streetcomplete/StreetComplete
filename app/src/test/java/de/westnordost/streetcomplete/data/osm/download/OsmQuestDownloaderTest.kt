package de.westnordost.streetcomplete.data.osm.download

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.osm.*
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.*
import java.util.*
import java.util.concurrent.FutureTask


class OsmQuestDownloaderTest {
    private lateinit var geometryDb: ElementGeometryDao
    private lateinit var elementDb: MergedElementDao
    private lateinit var osmQuestDao: OsmQuestDao
    private lateinit var countryBoundaries: CountryBoundaries
    private lateinit var downloader: OsmQuestDownloader
    private lateinit var listener: VisibleQuestListener

    @Before fun setUp() {
        geometryDb = mock()
        elementDb = mock()
        osmQuestDao = mock()
        countryBoundaries = mock()
        val countryBoundariesFuture = FutureTask { countryBoundaries }
        countryBoundariesFuture.run()
        listener = mock()
        downloader = OsmQuestDownloader(geometryDb, elementDb, osmQuestDao, countryBoundariesFuture)
        downloader.questListener = listener
    }

    @Test fun `ignore element with invalid geometry`() {
        val invalidGeometryElement = ElementWithGeometry(
            OsmNode(0, 0, OsmLatLon(1.0, 1.0), null),
            null
        )

        val questType = ListBackedQuestType(listOf(invalidGeometryElement))
        setPreviousQuests(emptyList())

        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0), setOf())

        verify(listener, times(0)).onQuestsCreated(any(), any())
    }

    @Test fun `ignore at blacklisted position`() {
        val blacklistPos = OsmLatLon(0.3, 0.4)
        val blacklistElement = ElementWithGeometry(
            OsmNode(0, 0, blacklistPos, null),
            ElementPointGeometry(blacklistPos)
        )

        val questType = ListBackedQuestType(listOf(blacklistElement))
        setPreviousQuests(emptyList())

        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0), setOf(blacklistPos))

        verify(listener, times(0)).onQuestsCreated(any(), any())
    }

    @Test fun `ignore element in country for which this quest is disabled`() {
        val pos = OsmLatLon(1.0, 1.0)
        val inDisabledCountryElement = ElementWithGeometry(
            OsmNode(0, 0, pos, null),
            ElementPointGeometry(pos)
        )

        val questType = ListBackedQuestType(listOf(inDisabledCountryElement))
        questType.enabledInCountries = AllCountriesExcept("AA")
        // country boundaries say that position is in AA
        on(countryBoundaries.isInAny(anyDouble(),anyDouble(),any())).thenReturn(true)
        on(countryBoundaries.getContainingIds(anyDouble(),anyDouble(),anyDouble(),anyDouble())).thenReturn(setOf())
        setPreviousQuests(emptyList())

        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0), setOf())

        verify(listener, times(0)).onQuestsCreated(any(), any())
    }

    @Test fun `creates quest for element`() {
        val pos = OsmLatLon(1.0, 1.0)
        val normalElement = ElementWithGeometry(
            OsmNode(0, 0, pos, null),
            ElementPointGeometry(pos)
        )

        val questType = ListBackedQuestType(listOf(normalElement))
        setPreviousQuests(emptyList())

        doAnswer { invocation ->
            val quests = invocation.arguments[0] as Collection<OsmQuest>
            var i = 0L
            for (quest in quests) {
                quest.id =  i++
            }
            quests.size
        }.on(osmQuestDao).addAll(any())

        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0), setOf())

        verify(listener).onQuestsCreated(any(), any())
        verify(geometryDb).putAll(any())
        verify(elementDb).putAll(any())
        verify(osmQuestDao).addAll(any())
    }

    @Test fun `deletes obsolete quests`() {
        val pos = OsmLatLon(3.0, 4.0)
        val node4 = ElementWithGeometry(
            OsmNode(4, 0, pos, null),
            ElementPointGeometry(pos)
        )
        // questType mock will only "find" the Node #4
        val questType = ListBackedQuestType(listOf(node4))
        // in the quest database mock, there are quests for node 4 and node 5
        setPreviousQuests(listOf(
            OsmQuest(12L,questType,Element.Type.NODE,4,QuestStatus.NEW,null,null,Date(),ElementPointGeometry(pos)),
            OsmQuest(13L,questType,Element.Type.NODE,5,QuestStatus.NEW,null,null,Date(),ElementPointGeometry(pos))
        ))

        // -> we expect that quest with node #5 is removed
        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0), emptySet())
        verify(osmQuestDao).deleteAllIds(listOf(13L))
        verify(listener).onQuestsRemoved(any(), any())
    }

    private fun setPreviousQuests(list: List<OsmQuest>) {
        on(osmQuestDao.getAll(isNull(), any(), isNull(), any(), isNull())).thenReturn(list)
    }
}

private data class ElementWithGeometry(val element: Element, val geometry: ElementGeometry?)

private class ListBackedQuestType(private val list: List<ElementWithGeometry>) : OsmElementQuestType<String> {

    override var enabledInCountries: Countries = AllCountries

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        for (e in list) {
            handler(e.element, e.geometry)
        }
        return true
    }

    override val icon = 0
    override val commitMessage = ""
    override fun getTitle(tags: Map<String, String>) = 0
    override fun createForm() = object : AbstractQuestAnswerFragment<String>() {}
    override fun isApplicableTo(element: Element) = false
    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {}
}
