package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.countryboundaries.CountryBoundaries
import org.junit.Before
import org.junit.Test

import java.util.Date

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.quest.*
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on

import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.verify
import java.util.concurrent.FutureTask

class OsmQuestGiverTest {

    private lateinit var blacklistedPositionsSource: BlacklistedPositionsSource
    private lateinit var blacklistedElementsSource: BlacklistedElementsSource
    private lateinit var osmQuestController: OsmQuestController
    private lateinit var questType: OsmElementQuestType<*>
    private lateinit var countryBoundaries: CountryBoundaries
    private lateinit var osmQuestGiver: OsmQuestGiver

    @Before fun setUp() {
        blacklistedPositionsSource = mock()
        on(blacklistedPositionsSource.getAllPositions(any())).thenReturn(emptyList())

        blacklistedElementsSource = mock()
        on(blacklistedElementsSource.getAll()).thenReturn(emptyList())

        osmQuestController = mock()
        on(osmQuestController.getAllForElement(Element.Type.NODE, 1)).thenReturn(emptyList())
        on(osmQuestController.updateForElement(any(), any(), any(), any(), anyLong())).thenReturn(OsmQuestController.UpdateResult(0,0))

        questType = mock()
        on(questType.enabledInCountries).thenReturn(AllCountries)
        on(questType.isApplicableTo(NODE)).thenReturn(true)

        countryBoundaries = mock()
        val future = FutureTask { countryBoundaries }
        future.run()

        val questTypeRegistry: QuestTypeRegistry = mock()
        on(questTypeRegistry.all).thenReturn(listOf(questType))

        osmQuestGiver = OsmQuestGiver(blacklistedPositionsSource, blacklistedElementsSource, osmQuestController, questTypeRegistry, future)
    }

    @Test fun `note blocks new quests`() {
        // there is a note at our position
        on(blacklistedPositionsSource.getAllPositions(any())).thenReturn(listOf(POS))

        osmQuestGiver.updateQuests(NODE, GEOM)

        verify(osmQuestController).updateForElement(emptyList(), emptyList(), GEOM, NODE.type, NODE.id)
    }

    @Test fun `element blocks new quests`() {
        on(blacklistedElementsSource.getAll()).thenReturn(listOf(ELEMENT_KEY))

        osmQuestGiver.updateQuests(NODE, GEOM)

        verify(osmQuestController).updateForElement(emptyList(), emptyList(), GEOM, NODE.type, NODE.id)
    }

    @Test fun `previous quest blocks new quest`() {
        // there is a quest for the given element already
        val q = OsmQuest(questType, NODE.type, NODE.id, ElementPointGeometry(POS))
        on(osmQuestController.getAllForElement(NODE.type, NODE.id)).thenReturn(listOf(q))

        osmQuestGiver.updateQuests(NODE, GEOM)

        verify(osmQuestController).updateForElement(emptyList(), emptyList(), GEOM, NODE.type, NODE.id)
    }

    @Test fun `not applicable blocks new quest`() {
        // our quest type is not applicable to the element
        on(questType.isApplicableTo(NODE)).thenReturn(false)

        osmQuestGiver.updateQuests(NODE, GEOM)

        verify(osmQuestController).updateForElement(emptyList(), emptyList(), GEOM, NODE.type, NODE.id)
    }

    @Test fun `not applicable removes previous quest`() {
        // there is a quest for the given element already
        val q = OsmQuest(123L, questType, NODE.type, NODE.id, QuestStatus.NEW, null, null, Date(), ElementPointGeometry(POS))
        on(osmQuestController.getAllForElement(Element.Type.NODE, 1)).thenReturn(listOf(q))
        // but it is not applicable to the element anymore
        on(questType.isApplicableTo(NODE)).thenReturn(false)

        osmQuestGiver.updateQuests(NODE, GEOM)

        verify(osmQuestController).updateForElement(emptyList(), listOf(123L), GEOM, NODE.type, NODE.id)
    }

    @Test fun `applicable adds new quest`() {
        // there is no quest before, the quest is applicable etc. (code in setUp())
        osmQuestGiver.updateQuests(NODE, GEOM)

        val expectedQuest = OsmQuest(questType, NODE.type, NODE.id, GEOM)
        verify(osmQuestController).updateForElement(arrayListOf(expectedQuest), emptyList(), GEOM, NODE.type, NODE.id)
    }

    @Test fun `quest is only enabled in the country the element is in`() {
        on(questType.enabledInCountries).thenReturn(NoCountriesExcept("DE"))
        on(countryBoundaries.isInAny(anyDouble(), anyDouble(), any())).thenReturn(true)

        osmQuestGiver.updateQuests(NODE, GEOM)

        val expectedQuest = OsmQuest(questType, NODE.type, NODE.id, GEOM)
        verify(osmQuestController).updateForElement(arrayListOf(expectedQuest), emptyList(), GEOM, NODE.type, NODE.id)
    }

    @Test fun `quest is disabled in a country the element is not in`() {
        on(questType.enabledInCountries).thenReturn(AllCountriesExcept("DE"))
        on(countryBoundaries.isInAny(anyDouble(), anyDouble(), any())).thenReturn(true)

        osmQuestGiver.updateQuests(NODE, GEOM)

        verify(osmQuestController).updateForElement(emptyList(), emptyList(), GEOM, NODE.type, NODE.id)
    }

    @Test fun `recreate quests`() {
        val questType2: OsmElementQuestType<*> = mock()
        osmQuestGiver.recreateQuests(NODE, GEOM, listOf(questType, questType2))

        val expectedQuests = listOf(
            OsmQuest(questType, NODE.type, NODE.id, GEOM),
            OsmQuest(questType2, NODE.type, NODE.id, GEOM)
        )
        verify(osmQuestController).updateForElement(expectedQuests, emptyList(), GEOM, NODE.type, NODE.id)
    }
}

private val POS = OsmLatLon(10.0, 10.0)
private val NODE = OsmNode(1, 0, POS, null, null, null)
private val ELEMENT_KEY = ElementKey(Element.Type.NODE, 1)
private val GEOM = ElementPointGeometry(POS)
