package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify

class OsmElementUpdateControllerTest {

    private lateinit var mapDataApi: MapDataApi
    private lateinit var elementGeometryCreator: ElementGeometryCreator
    private lateinit var elementDB: MergedElementDao
    private lateinit var questGiver: OsmQuestGiver
    private lateinit var c: OsmElementUpdateController

    @Before fun setUp() {
        mapDataApi = mock()
        elementGeometryCreator = mock()
        elementDB = mock()
        questGiver = mock()
        c = OsmElementUpdateController(mapDataApi, elementGeometryCreator, elementDB, questGiver)
    }

    @Test fun delete() {
        c.delete(Element.Type.NODE, 123L)

        verify(elementDB).delete(Element.Type.NODE, 123L)
        verify(questGiver).deleteQuests(Element.Type.NODE, 123L)
    }

    @Test fun update() {
        val element = OsmNode(123L, 1, 0.0, 0.0, null)
        val point = ElementPointGeometry(element.position)

        on(elementGeometryCreator.create(element)).thenReturn(point)

        c.update(element, null)

        verify(elementDB).put(element)
        verify(elementGeometryCreator).create(element)
        verify(questGiver).updateQuests(element, point)
    }

    @Test fun `update deleted`() {
        val element = OsmNode(123L, 1, 0.0, 0.0, null)

        on(elementGeometryCreator.create(element)).thenReturn(null)

        c.update(element, null)

        verify(elementDB).delete(element.type, element.id)
        verify(questGiver).deleteQuests(element.type, element.id)
    }

    @Test fun recreate() {
        val element = OsmNode(123L, 1, 0.0, 0.0, null)
        val point = ElementPointGeometry(element.position)
        val questType: OsmElementQuestType<Boolean> = mock()
        val questTypes = listOf(questType)
        on(elementGeometryCreator.create(element)).thenReturn(point)

        c.update(element, questTypes)


        verify(elementDB).put(element)
        verify(elementGeometryCreator).create(element)
        verify(questGiver).recreateQuests(element, point, questTypes)
    }
}