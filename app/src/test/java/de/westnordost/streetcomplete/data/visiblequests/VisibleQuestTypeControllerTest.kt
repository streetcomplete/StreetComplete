package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeDisabled
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class VisibleQuestTypeControllerTest {

    private lateinit var visibleQuestTypeDao: VisibleQuestTypeDao
    private lateinit var questPresetsSource: QuestPresetsSource
    private lateinit var ctrl: VisibleQuestTypeController
    private lateinit var listener: VisibleQuestTypeSource.Listener

    private lateinit var questPresetsListener: QuestPresetsSource.Listener

    private val disabledQuest = TestQuestTypeDisabled()
    private val quest1 = TestQuestTypeA()
    private val quest2 = TestQuestTypeB()

    @Before fun setUp() {
        visibleQuestTypeDao = mock()
        questPresetsSource = mock()

        on(questPresetsSource.addListener(any())).then { invocation ->
            questPresetsListener = (invocation.arguments[0] as QuestPresetsSource.Listener)
            Unit
        }

        on(questPresetsSource.selectedQuestPresetId).thenReturn(0)

        ctrl = VisibleQuestTypeController(visibleQuestTypeDao, questPresetsSource)

        listener = mock()
        ctrl.addListener(listener)

    }

    @Test fun `default visibility`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf())
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
    }

    @Test fun `get visibility`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        ))
        assertFalse(ctrl.isVisible(quest1))
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
        ctrl.setVisible(quest1, true)
        assertTrue(ctrl.isVisible(quest1))
        verify(visibleQuestTypeDao).put(0, quest1.name, true)
        verify(listener).onQuestTypeVisibilityChanged(quest1, true)
    }

    @Test fun `set visibility of several`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to true
        ))
        ctrl.setAllVisible(listOf(quest1, quest2), false)
        assertFalse(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(quest2))
        verify(visibleQuestTypeDao).put(
            eq(0),
            eq(listOf(quest1.name, quest2.name)),
            eq(false)
        )
        verify(listener).onQuestTypeVisibilitiesChanged()
    }

    @Test fun `clear visibilities`() {
        on(visibleQuestTypeDao.getAll(0)).thenReturn(mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        ))
        ctrl.clear()
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        verify(visibleQuestTypeDao).clear(0)
        verify(listener).onQuestTypeVisibilitiesChanged()
    }

    @Test fun `clears visibilities of deleted quest preset`() {
        questPresetsListener.onDeletedQuestPreset(1)
        verify(visibleQuestTypeDao).clear(1)
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

private val QuestType<*>.name get() = this::class.simpleName!!
