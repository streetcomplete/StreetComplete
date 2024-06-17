package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class QuestPresetControllerTest {

    @Mock private lateinit var questPresetsDao: QuestPresetsDao
    @Mock private lateinit var selectedQuestPresetStore: SelectedQuestPresetStore
    private lateinit var ctrl: QuestPresetsController
    @Mock private lateinit var listener: QuestPresetsSource.Listener

    private val preset = QuestPreset(1, "test")

    @BeforeTest fun setUp() {
        questPresetsDao = mock(classOf<QuestPresetsDao>())
        selectedQuestPresetStore = mock(classOf<SelectedQuestPresetStore>())
        ctrl = QuestPresetsController(questPresetsDao, selectedQuestPresetStore)

        listener = mock(classOf<QuestPresetsSource.Listener>())
        ctrl.addListener(listener)
    }

    @Test fun get() {
every { questPresetsDao.getName(1) }.returns("huhu")
every { selectedQuestPresetStore.get() }.returns(1)
        assertEquals("huhu", ctrl.selectedQuestPresetName)
    }

    @Test fun getAll() {
every { questPresetsDao.getAll() }.returns(listOf(preset))
        assertEquals(listOf(preset), ctrl.getAll())
    }

    @Test fun add() {
every { questPresetsDao.add(any()) }.returns(123)
        ctrl.add("test")
verifyInvokedExactlyOnce { questPresetsDao.add("test") }
verifyInvokedExactlyOnce { listener.onAddedQuestPreset(QuestPreset(123, "test")) }
    }

    @Test fun delete() {
        every { selectedQuestPresetStore.get() }.returns(123)
        ctrl.delete(123)
verifyInvokedExactlyOnce { questPresetsDao.delete(123) }
verifyInvokedExactlyOnce { listener.onDeletedQuestPreset(123) }
    }

    @Test fun `delete current preset switches to preset 0`() {
every { ctrl.selectedId }.returns(55)
        ctrl.delete(55)
verifyInvokedExactlyOnce { questPresetsDao.delete(55) }
verifyInvokedExactlyOnce { listener.onDeletedQuestPreset(55) }
verifyInvokedExactlyOnce { selectedQuestPresetStore.set(0) }
    }

    @Test fun `change current preset`() {
        ctrl.selectedId = 11
verifyInvokedExactlyOnce { selectedQuestPresetStore.set(11) }
verifyInvokedExactlyOnce { listener.onSelectedQuestPresetChanged() }
    }
}
