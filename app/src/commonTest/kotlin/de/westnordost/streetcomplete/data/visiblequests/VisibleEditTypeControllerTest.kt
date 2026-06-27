package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.data.quest.TestQuestTypeB
import de.westnordost.streetcomplete.data.quest.TestQuestTypeDisabled
import dev.mokkery.answering.calls
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifyNoMoreCalls
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
        editTypePresetsSource = mock() {
            every { addListener(any()) } calls { (listener: EditTypePresetsSource.Listener) ->
                editTypePresetsListener = listener
            }
            every { selectedId } returns 0
        }
        allEditTypes = AllEditTypes(listOf(
            QuestTypeRegistry(listOf(0 to quest1, 1 to quest2, 2 to disabledQuest)),
        ))

        ctrl = VisibleEditTypeController(visibleEditTypeDao, editTypePresetsSource, allEditTypes)

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun `default visibility`() {
        every { visibleEditTypeDao.getAll(0) } returns mutableMapOf()
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        assertEquals(setOf(quest1, quest2), ctrl.getVisible())
    }

    @Test fun `get visibility`() {
        every { visibleEditTypeDao.getAll(0) } returns mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        )
        assertFalse(ctrl.isVisible(quest1))
        assertEquals(setOf(quest2, disabledQuest), ctrl.getVisible())
        assertTrue(ctrl.isVisible(disabledQuest))
    }

    @Test fun `visibility is cached`() {
        every { visibleEditTypeDao.getAll(0) } returns mutableMapOf(quest1.name to false)
        ctrl.isVisible(quest1)
        ctrl.isVisible(quest1)
        ctrl.isVisible(quest1)
        verify(exactly(1)) { visibleEditTypeDao.getAll(0) }
    }

    @Test fun `set visibility`() {
        every { visibleEditTypeDao.getAll(0) } returns mutableMapOf(quest1.name to false)
        ctrl.setVisibility(quest1, true)
        assertTrue(ctrl.isVisible(quest1))
        verify { visibleEditTypeDao.put(0, quest1.name, true) }
        verify { listener.onVisibilityChanged(quest1, true) }
    }

    @Test fun `set visibility in non-selected preset`() {
        every { visibleEditTypeDao.getAll(1) } returns mutableMapOf(quest1.name to false)
        ctrl.setVisibility(quest1, true, 1)
        verify { visibleEditTypeDao.put(1, quest1.name, true) }
        verifyNoMoreCalls(listener)
    }

    @Test fun `set visibility of several`() {
        every { visibleEditTypeDao.getAll(0) } returns mutableMapOf(quest1.name to true)
        ctrl.setVisibilities(mapOf(quest1 to false, quest2 to false))
        assertFalse(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(quest2))
        verify { visibleEditTypeDao.putAll(0, mapOf(quest1.name to false, quest2.name to false)) }
        verify { listener.onVisibilitiesChanged() }
    }

    @Test fun `set visibility of several in non-selected preset`() {
        every { visibleEditTypeDao.getAll(1) } returns mutableMapOf(quest1.name to true)
        ctrl.setVisibilities(mapOf(quest1 to false, quest2 to false), 1)
        verify { visibleEditTypeDao.putAll(1, mapOf(quest1.name to false, quest2.name to false)) }
        verifyNoMoreCalls(listener)
    }

    @Test fun `clear visibilities`() {
        every { visibleEditTypeDao.getAll(0) } returns mutableMapOf(
            quest1.name to false,
            disabledQuest.name to true
        )
        ctrl.clearVisibilities(allEditTypes)
        assertTrue(ctrl.isVisible(quest1))
        assertFalse(ctrl.isVisible(disabledQuest))
        assertEquals(setOf(quest1, quest2), ctrl.getVisible())
        verify { visibleEditTypeDao.clear(0, allEditTypes.map { it.name }) }
        verify { listener.onVisibilitiesChanged() }
    }

    @Test fun `clears visibilities of deleted edit type preset`() {
        editTypePresetsListener.onDeleted(1)
        verify { visibleEditTypeDao.clear(1, allEditTypes.map { it.name }) }
        verifyNoMoreCalls(listener)
    }

    @Test fun `clears cache and notifies listener when changing edit type preset`() {
        // make sure that visibilities are queried once from DB
        every { visibleEditTypeDao.getAll(0) } returns mutableMapOf()
        assertTrue(ctrl.isVisible(quest1))

        editTypePresetsListener.onSelectionChanged()
        verify { listener.onVisibilitiesChanged() }

        // now they should be queried again: we expect getAll to be called twice
        assertTrue(ctrl.isVisible(quest1))
        verify(exactly(2)) { visibleEditTypeDao.getAll(0) }
    }
}
