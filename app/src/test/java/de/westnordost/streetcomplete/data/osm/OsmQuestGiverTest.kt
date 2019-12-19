package de.westnordost.streetcomplete.data.osm

import de.westnordost.countryboundaries.CountryBoundaries
import org.junit.Before
import org.junit.Test

import java.util.Date

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on

import org.junit.Assert.*
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.verify
import java.util.concurrent.FutureTask

class OsmQuestGiverTest {

    private lateinit var osmNoteQuestDao: OsmNoteQuestDao
    private lateinit var osmQuestDao: OsmQuestDao
    private lateinit var osmQuestUnlocker: OsmQuestGiver
    private lateinit var questType: OsmElementQuestType<*>
    private lateinit var countryBoundaries: CountryBoundaries

    @Before fun setUp() {
        val elementGeometryDao: ElementGeometryDao = mock()
        on(elementGeometryDao.get(Element.Type.NODE, 1)).thenReturn(ElementPointGeometry(POS))

        osmNoteQuestDao = mock()
        on(osmNoteQuestDao.getAllPositions(any())).thenReturn(emptyList())

        osmQuestDao = mock()
        on(osmQuestDao.getAll(element = ElementKey(Element.Type.NODE, 1))).thenReturn(emptyList())

        questType = mock()
        on(questType.enabledInCountries).thenReturn(AllCountries)

        countryBoundaries = mock()
        val future = FutureTask { countryBoundaries }
        future.run()

        val questTypeProvider: OrderedVisibleQuestTypesProvider = mock()
        on(questTypeProvider.get()).thenReturn(listOf(questType))

        osmQuestUnlocker = OsmQuestGiver(osmNoteQuestDao, osmQuestDao, elementGeometryDao, questTypeProvider, future)
    }

    @Test fun `note blocks new quests`() {
        on(questType.isApplicableTo(NODE)).thenReturn(true)
        on(osmNoteQuestDao.getAllPositions(any())).thenReturn(listOf(POS))

        assertTrue(osmQuestUnlocker.updateQuests(NODE).createdQuests.isEmpty())
    }

    @Test fun `previous quest blocks new quest`() {
        val q = OsmQuest(questType, Element.Type.NODE, 1, ElementPointGeometry(POS))
        on(osmQuestDao.getAll(element = ElementKey(Element.Type.NODE, 1))).thenReturn(listOf(q))
        on(questType.isApplicableTo(NODE)).thenReturn(true)

        val r = osmQuestUnlocker.updateQuests(NODE)
        assertTrue(r.createdQuests.isEmpty())
        assertTrue(r.removedQuestIds.isEmpty())
    }

    @Test fun `not applicable blocks new quest`() {
        on(questType.isApplicableTo(NODE)).thenReturn(false)

        val r = osmQuestUnlocker.updateQuests(NODE)
        assertTrue(r.createdQuests.isEmpty())
        assertTrue(r.removedQuestIds.isEmpty())
    }

    @Test fun `not applicable removes previous quest`() {
        val q = OsmQuest(123L, questType, Element.Type.NODE, 1, QuestStatus.NEW, null, null, Date(), ElementPointGeometry(POS))
        on(osmQuestDao.getAll(element = ElementKey(Element.Type.NODE, 1))).thenReturn(listOf(q))
        on(questType.isApplicableTo(NODE)).thenReturn(false)

        val r = osmQuestUnlocker.updateQuests(NODE)
        assertTrue(r.createdQuests.isEmpty())
        assertEquals(123L, r.removedQuestIds.single())

        verify(osmQuestDao).deleteAllIds(listOf(123L))
    }

    @Test fun `applicable adds new quest`() {
        on(questType.isApplicableTo(NODE)).thenReturn(true)
        val r = osmQuestUnlocker.updateQuests(NODE)
        val quest = r.createdQuests.single()
        assertEquals(1, quest.elementId)
        assertEquals(Element.Type.NODE, quest.elementType)
        assertEquals(questType, quest.type)

        verify(osmQuestDao).deleteAll(statusIn = listOf(QuestStatus.REVERT), element = ElementKey(Element.Type.NODE, 1))
        verify(osmQuestDao).addAll(listOf(quest))
    }

    @Test fun `quest is only enabled in the country the element is in`() {
        on(questType.isApplicableTo(NODE)).thenReturn(true)
        on(questType.enabledInCountries).thenReturn(NoCountriesExcept("DE"))
        on(countryBoundaries.isInAny(anyDouble(), anyDouble(), any())).thenReturn(true)

        assertEquals(1, osmQuestUnlocker.updateQuests(NODE).createdQuests.size)
    }

    @Test fun `quest is only enabled in a country the element is not in`() {
        on(questType.isApplicableTo(NODE)).thenReturn(true)
        on(questType.enabledInCountries).thenReturn(NoCountriesExcept("DE"))
        on(countryBoundaries.isInAny(anyDouble(), anyDouble(), any())).thenReturn(false)

        assertTrue(osmQuestUnlocker.updateQuests(NODE).createdQuests.isEmpty())
    }

    @Test fun `quest is disabled in the country the element is in`() {
        on(questType.isApplicableTo(NODE)).thenReturn(true)
        on(questType.enabledInCountries).thenReturn(AllCountriesExcept("DE"))
        on(countryBoundaries.isInAny(anyDouble(), anyDouble(), any())).thenReturn(true)

        assertTrue(osmQuestUnlocker.updateQuests(NODE).createdQuests.isEmpty())
    }

    @Test fun `quest is disabled in the country the element is not in`() {
        on(questType.isApplicableTo(NODE)).thenReturn(true)
        on(questType.enabledInCountries).thenReturn(AllCountriesExcept("DE"))
        on(countryBoundaries.isInAny(anyDouble(), anyDouble(), any())).thenReturn(false)

        assertEquals(1, osmQuestUnlocker.updateQuests(NODE).createdQuests.size)
    }
}

private val POS = OsmLatLon(10.0, 10.0)
private val NODE = OsmNode(1, 0, POS, null, null, null)
