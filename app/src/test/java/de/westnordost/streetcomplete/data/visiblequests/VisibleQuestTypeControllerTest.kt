package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeDisabled
import de.westnordost.streetcomplete.testutils.verifyInvokedExactly
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.eq
import io.mockative.every
import io.mockative.mock

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VisibleQuestTypeControllerTest {

    @Mock private lateinit var visibleQuestTypeDao: VisibleQuestTypeDao
    @Mock private lateinit var questPresetsSource: QuestPresetsSource
    @Mock private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var ctrl: VisibleQuestTypeController
    @Mock private lateinit var listener: VisibleQuestTypeSource.Listener

    private lateinit var questPresetsListener: QuestPresetsSource.Listener

    private val disabledQuest = TestQuestTypeDisabled()
    private val quest1 = TestQuestTypeA()
    private val quest2 = TestQuestTypeB()

    @BeforeTest fun setUp() {
        visibleQuestTypeDao = mock(classOf<VisibleQuestTypeDao>())
        questPresetsSource = mock(classOf<QuestPresetsSource>())
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to quest1,
            1 to quest2,
            2 to disabledQuest
        ))

        every { questPresetsSource.addListener(any()) }.invokes { args ->
            questPresetsListener = (args[0] as QuestPresetsSource.Listener)
            Unit
        }

        every { questPresetsSource.selectedId }.returns(0)

        ctrl = VisibleQuestTypeController(visibleQuestTypeDao, questPresetsSource, questTypeRegistry)

        listener = mock(classOf<VisibleQuestTypeSource.Listener>())
        ctrl.addListener(listener)
    }

    @Test fun `default visibility`() {
        every { visibleQuestTypeDao.getAll(0) }.returns(mutableMapOf())
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        assertEquals(setOf(quest1, quest2), ctrl.getVisible())
    }

    @Test fun `get visibility`() {
        every { visibleQuestTypeDao.getAll(0) }.returns(mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        ))
        assertFalse(ctrl.isVisible(quest1))
        assertEquals(setOf(quest2, disabledQuest), ctrl.getVisible())
        assertTrue(ctrl.isVisible(disabledQuest))
    }

    @Test fun `visibility is cached`() {
        every {visibleQuestTypeDao.getAll(0)}.returns(mutableMapOf(
            quest1.name to false
        ))
        ctrl.isVisible(quest1)
        ctrl.isVisible(quest1)
        ctrl.isVisible(quest1)
        verifyInvokedExactlyOnce { visibleQuestTypeDao.getAll(0) }
    }

    @Test fun `set visibility`() {
        every { visibleQuestTypeDao.getAll(0)}.returns(mutableMapOf(
            quest1.name to false
        ))
        ctrl.setVisibility(quest1, true)
        assertTrue(ctrl.isVisible(quest1))
        verifyInvokedExactlyOnce { visibleQuestTypeDao.put(0, quest1.name, true) }
        verifyInvokedExactlyOnce { listener.onQuestTypeVisibilityChanged(quest1, true) }
    }

    @Test fun `set visibility in non-selected preset`() {
        every {visibleQuestTypeDao.getAll(1) }.returns(mutableMapOf(
            quest1.name to false
        ))
        ctrl.setVisibility(quest1, true, 1)
        verifyInvokedExactlyOnce { visibleQuestTypeDao.put(1, quest1.name, true) }
        // TODO verifyNoInteractions(listener)
    }

    @Test fun `set visibility of several`() {
        every { visibleQuestTypeDao.getAll(0) }.returns(mutableMapOf(
            quest1.name to true
        ))
        ctrl.setVisibilities(mapOf(quest1 to false, quest2 to false))
        assertFalse(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(quest2))
        verifyInvokedExactlyOnce { visibleQuestTypeDao.putAll(
            eq(0),
            eq(mapOf(quest1.name to false, quest2.name to false))
        )}
        verifyInvokedExactlyOnce { listener.onQuestTypeVisibilitiesChanged() }
    }

    @Test fun `set visibility of several in non-selected preset`() {
        every { visibleQuestTypeDao.getAll(1) }.returns(mutableMapOf(
            quest1.name to true
        ))
        ctrl.setVisibilities(mapOf(quest1 to false, quest2 to false), 1)
        verifyInvokedExactlyOnce { visibleQuestTypeDao.putAll(
            eq(1),
            eq(mapOf(quest1.name to false, quest2.name to false))
        )}
        // TODO verifyNoInteractions(listener)
    }

    @Test fun `clear visibilities`() {
        every { visibleQuestTypeDao.getAll(0) }.returns(mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        ))
        ctrl.clearVisibilities()
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        assertEquals(setOf(quest1, quest2), ctrl.getVisible())
        verifyInvokedExactlyOnce { visibleQuestTypeDao.clear(0) }
        verifyInvokedExactlyOnce { listener.onQuestTypeVisibilitiesChanged() }
    }

    @Test fun `clears visibilities of deleted quest preset`() {
        questPresetsListener.onDeletedQuestPreset(1)
        verifyInvokedExactlyOnce { visibleQuestTypeDao.clear(1) }
        // TODO verifyNoInteractions(listener)
    }

    @Test fun `clears cache and notifies listener when changing quest preset`() {
        // make sure that visibilities are queried once from DB
        every { visibleQuestTypeDao.getAll(0) }.returns(mutableMapOf())
        assertTrue(ctrl.isVisible(quest1))

        questPresetsListener.onSelectedQuestPresetChanged()
        verifyInvokedExactlyOnce { listener.onQuestTypeVisibilitiesChanged() }

        // now they should be queried again: we expect getAll to be called twice
        assertTrue(ctrl.isVisible(quest1))
        verifyInvokedExactly(2) { visibleQuestTypeDao.getAll(0) }
    }
}
