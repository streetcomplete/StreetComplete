package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeDisabled
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VisibleEditTypeControllerTest {

    private lateinit var visibleEditTypeDao: VisibleEditTypeDao
    private lateinit var editTypePresetsSource: EditTypePresetsSource
    private lateinit var allEditTypes: AllEditTypes
    private lateinit var ctrl: VisibleEditTypeController
    private lateinit var listener: VisibleEditTypeSource.Listener

    private lateinit var editTypePresetsListener: EditTypePresetsSource.Listener

    private val disabledQuest = TestQuestTypeDisabled()
    private val quest1 = TestQuestTypeA()
    private val quest2 = TestQuestTypeB()

    @BeforeTest fun setUp() {
        visibleEditTypeDao = mock()
        editTypePresetsSource = mock()
        allEditTypes = AllEditTypes(listOf(
            QuestTypeRegistry(listOf(0 to quest1, 1 to quest2, 2 to disabledQuest)),
        ))

        on(editTypePresetsSource.addListener(any())).then { invocation ->
            editTypePresetsListener = (invocation.arguments[0] as EditTypePresetsSource.Listener)
            Unit
        }

        on(editTypePresetsSource.selectedId).thenReturn(0)

        ctrl = VisibleEditTypeController(visibleEditTypeDao, editTypePresetsSource, allEditTypes)

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun `default visibility`() {
        on(visibleEditTypeDao.getAll(0)).thenReturn(mutableMapOf())
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        assertEquals(setOf(quest1, quest2), ctrl.getVisible())
    }

    @Test fun `get visibility`() {
        on(visibleEditTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        ))
        assertFalse(ctrl.isVisible(quest1))
        assertEquals(setOf(quest2, disabledQuest), ctrl.getVisible())
        assertTrue(ctrl.isVisible(disabledQuest))
    }

    @Test fun `visibility is cached`() {
        on(visibleEditTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false
        ))
        ctrl.isVisible(quest1)
        ctrl.isVisible(quest1)
        ctrl.isVisible(quest1)
        verify(visibleEditTypeDao, times(1)).getAll(0)
    }

    @Test fun `set visibility`() {
        on(visibleEditTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false
        ))
        ctrl.setVisibility(quest1, true)
        assertTrue(ctrl.isVisible(quest1))
        verify(visibleEditTypeDao).put(0, quest1.name, true)
        verify(listener).onVisibilityChanged(quest1, true)
    }

    @Test fun `set visibility in non-selected preset`() {
        on(visibleEditTypeDao.getAll(1)).thenReturn(mutableMapOf(
            quest1.name to false
        ))
        ctrl.setVisibility(quest1, true, 1)
        verify(visibleEditTypeDao).put(1, quest1.name, true)
        verifyNoInteractions(listener)
    }

    @Test fun `set visibility of several`() {
        on(visibleEditTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to true
        ))
        ctrl.setVisibilities(mapOf(quest1 to false, quest2 to false))
        assertFalse(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(quest2))
        verify(visibleEditTypeDao).putAll(
            eq(0),
            eq(mapOf(quest1.name to false, quest2.name to false))
        )
        verify(listener).onVisibilitiesChanged()
    }

    @Test fun `set visibility of several in non-selected preset`() {
        on(visibleEditTypeDao.getAll(1)).thenReturn(mutableMapOf(
            quest1.name to true
        ))
        ctrl.setVisibilities(mapOf(quest1 to false, quest2 to false), 1)
        verify(visibleEditTypeDao).putAll(
            eq(1),
            eq(mapOf(quest1.name to false, quest2.name to false))
        )
        verifyNoInteractions(listener)
    }

    @Test fun `clear visibilities`() {
        on(visibleEditTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        ))
        ctrl.clearVisibilities(allEditTypes)
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        assertEquals(setOf(quest1, quest2), ctrl.getVisible())
        verify(visibleEditTypeDao).clear(0, allEditTypes.map { it.name })
        verify(listener).onVisibilitiesChanged()
    }

    @Test fun `clears visibilities of deleted edit type preset`() {
        editTypePresetsListener.onDeleted(1)
        verify(visibleEditTypeDao).clear(1, allEditTypes.map { it.name })
        verifyNoInteractions(listener)
    }

    @Test fun `clears cache and notifies listener when changing edit type preset`() {
        // make sure that visibilities are queried once from DB
        on(visibleEditTypeDao.getAll(0)).thenReturn(mutableMapOf())
        assertTrue(ctrl.isVisible(quest1))

        editTypePresetsListener.onSelectionChanged()
        verify(listener).onVisibilitiesChanged()

        // now they should be queried again: we expect getAll to be called twice
        assertTrue(ctrl.isVisible(quest1))
        verify(visibleEditTypeDao, times(2)).getAll(0)
    }
}
