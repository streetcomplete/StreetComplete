package de.westnordost.streetcomplete.data.visiblequests

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

class VisibleQuestTypeControllerTest {

    private lateinit var visibleQuestTypeDao: VisibleQuestTypeDao
    private lateinit var questPresetsSource: QuestPresetsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var ctrl: VisibleQuestTypeController
    private lateinit var listener: VisibleQuestTypeSource.Listener

    private lateinit var questPresetsListener: QuestPresetsSource.Listener

    private val disabledQuest = TestQuestTypeDisabled()
    private val quest1 = TestQuestTypeA()
    private val quest2 = TestQuestTypeB()

    @BeforeTest fun setUp() {
        visibleQuestTypeDao = mock()
        questPresetsSource = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to quest1,
            1 to quest2,
            2 to disabledQuest
        ))

        on(questPresetsSource.addListener(any())).then { invocation ->
            questPresetsListener = (invocation.arguments[0] as QuestPresetsSource.Listener)
            Unit
        }

        on(questPresetsSource.selectedId).thenReturn(0)

        ctrl = VisibleQuestTypeController(visibleQuestTypeDao, questPresetsSource, questTypeRegistry)

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun `default visibility`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf())
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        assertEquals(setOf(quest1, quest2), ctrl.getVisible())
    }

    @Test fun `get visibility`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        ))
        assertFalse(ctrl.isVisible(quest1))
        assertEquals(setOf(quest2, disabledQuest), ctrl.getVisible())
        assertTrue(ctrl.isVisible(disabledQuest))
    }

    @Test fun `visibility is cached`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false
        ))
        ctrl.isVisible(quest1)
        ctrl.isVisible(quest1)
        ctrl.isVisible(quest1)
        verify(visibleQuestTypeDao, times(1)).getAll(0)
    }

    @Test fun `set visibility`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false
        ))
        ctrl.setVisibility(quest1, true)
        assertTrue(ctrl.isVisible(quest1))
        verify(visibleQuestTypeDao).put(0, quest1.name, true)
        verify(listener).onQuestTypeVisibilityChanged(quest1, true)
    }

    @Test fun `set visibility in non-selected preset`() {
        on(visibleQuestTypeDao.getAll(1)).thenReturn(mutableMapOf(
            quest1.name to false
        ))
        ctrl.setVisibility(quest1, true, 1)
        verify(visibleQuestTypeDao).put(1, quest1.name, true)
        verifyNoInteractions(listener)
    }

    @Test fun `set visibility of several`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to true
        ))
        ctrl.setVisibilities(mapOf(quest1 to false, quest2 to false))
        assertFalse(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(quest2))
        verify(visibleQuestTypeDao).putAll(
            eq(0),
            eq(mapOf(quest1.name to false, quest2.name to false))
        )
        verify(listener).onQuestTypeVisibilitiesChanged()
    }

    @Test fun `set visibility of several in non-selected preset`() {
        on(visibleQuestTypeDao.getAll(1)).thenReturn(mutableMapOf(
            quest1.name to true
        ))
        ctrl.setVisibilities(mapOf(quest1 to false, quest2 to false), 1)
        verify(visibleQuestTypeDao).putAll(
            eq(1),
            eq(mapOf(quest1.name to false, quest2.name to false))
        )
        verifyNoInteractions(listener)
    }

    @Test fun `clear visibilities`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        ))
        ctrl.clearVisibilities()
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        assertEquals(setOf(quest1, quest2), ctrl.getVisible())
        verify(visibleQuestTypeDao).clear(0)
        verify(listener).onQuestTypeVisibilitiesChanged()
    }

    @Test fun `clears visibilities of deleted quest preset`() {
        questPresetsListener.onDeletedQuestPreset(1)
        verify(visibleQuestTypeDao).clear(1)
        verifyNoInteractions(listener)
    }

    @Test fun `clears cache and notifies listener when changing quest preset`() {
        // make sure that visibilities are queried once from DB
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf())
        assertTrue(ctrl.isVisible(quest1))

        questPresetsListener.onSelectedQuestPresetChanged()
        verify(listener).onQuestTypeVisibilitiesChanged()

        // now they should be queried again: we expect getAll to be called twice
        assertTrue(ctrl.isVisible(quest1))
        verify(visibleQuestTypeDao, times(2)).getAll(0)
    }
}
