package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.testutils.QUEST_TYPE
import de.westnordost.streetcomplete.testutils.argThat
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OsmQuestsHiddenControllerTest {

    private lateinit var db: OsmQuestsHiddenDao
    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry

    private lateinit var ctrl: OsmQuestsHiddenController

    private lateinit var listener: OsmQuestsHiddenSource.Listener

    @BeforeTest fun setUp() {
        db = mock()
        mapDataSource = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to QUEST_TYPE,
        ))

        listener = mock()
        ctrl = OsmQuestsHiddenController(db, mapDataSource, questTypeRegistry)
        ctrl.addListener(listener)
    }

    @Test fun get() {
        val hiddenQuest = OsmQuestKey(NODE, 2, QUEST_TYPE.name)
        val g = pGeom()
        on(db.getTimestamp(hiddenQuest)).thenReturn(123)
        on(mapDataSource.getGeometry(NODE, 2)).thenReturn(g)

        assertEquals(
            OsmQuestHidden(NODE, 2, QUEST_TYPE, g, 123),
            ctrl.get(hiddenQuest)
        )
    }

    @Test fun isHidden() {
        val q1 = OsmQuestKey(NODE, 1, QUEST_TYPE.name)
        val q2 = OsmQuestKey(NODE, 2, QUEST_TYPE.name)
        on(db.contains(q1)).thenReturn(true)
        on(db.contains(q2)).thenReturn(false)

        assertTrue(ctrl.isHidden(q1))
        assertFalse(ctrl.isHidden(q2))
    }

    @Test fun getAllNewerThan() {
        val geoms = listOf(
            ElementPointGeometry(p()),
            ElementPointGeometry(p()),
            ElementPointGeometry(p()),
        )

        on(db.getNewerThan(123L)).thenReturn(listOf(
            // ok!
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 1L, QUEST_TYPE.name), 250),
            // unknown quest type
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 2L, "UnknownQuestType"), 250),
            // no geometry!
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 3L, QUEST_TYPE.name), 250),
        ))
        on(mapDataSource.getGeometries(argThat {
            it.containsExactlyInAnyOrder(listOf(
                ElementKey(NODE, 1),
                ElementKey(NODE, 2),
                ElementKey(NODE, 3)
            ))
        })).thenReturn(listOf(
            ElementGeometryEntry(NODE, 1, geoms[0]),
            ElementGeometryEntry(NODE, 2, geoms[1])
        ))

        assertEquals(
            listOf(OsmQuestHidden(NODE, 1, QUEST_TYPE, pGeom(), 250)),
            ctrl.getAllNewerThan(123L)
        )
    }


    @Test fun countAll() {
        on(db.countAll()).thenReturn(123L)
        assertEquals(123L, ctrl.countAll())
    }

    @Test fun hide() {
        val quest = osmQuest()

        on(db.getTimestamp(eq(quest.key))).thenReturn(555)
        on(mapDataSource.getGeometry(quest.elementType, quest.elementId)).thenReturn(pGeom())

        ctrl.hide(quest.key)

        verify(db).add(quest.key)
        verify(listener).onHid(eq(
            OsmQuestHidden(quest.elementType, quest.elementId, quest.type, quest.geometry, 555)
        ))
    }


    @Test fun unhide() {
        val quest = osmQuest()
        on(db.delete(quest.key)).thenReturn(true)
        on(db.getTimestamp(eq(quest.key))).thenReturn(555)
        on(mapDataSource.getGeometry(quest.elementType, quest.elementId)).thenReturn(pGeom())

        assertTrue(ctrl.unhide(quest.key))

        verify(db).delete(quest.key)
        verify(listener).onUnhid(eq(OsmQuestHidden(
            quest.elementType, quest.elementId, quest.type, quest.geometry, 555
        )))
    }

    @Test fun unhideAll() {
        on(db.deleteAll()).thenReturn(2)
        assertEquals(2, ctrl.unhideAll())
        verify(listener).onUnhidAll()
    }
}
