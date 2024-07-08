package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class QuestPresetControllerTest {

    private lateinit var questPresetsDao: QuestPresetsDao
    private lateinit var prefs: Preferences
    private lateinit var ctrl: QuestPresetsController
    private lateinit var listener: QuestPresetsSource.Listener

    private val preset = QuestPreset(1, "test")

    @BeforeTest fun setUp() {
        questPresetsDao = mock()
        prefs = mock()
        ctrl = QuestPresetsController(questPresetsDao, prefs)

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun get() {
        on(questPresetsDao.getName(1)).thenReturn("huhu")
        on(prefs.selectedQuestPreset).thenReturn(1)
        assertEquals("huhu", ctrl.selectedQuestPresetName)
    }

    @Test fun getAll() {
        on(questPresetsDao.getAll()).thenReturn(listOf(preset))
        assertEquals(listOf(preset), ctrl.getAll())
    }

    @Test fun add() {
        on(questPresetsDao.add(any())).thenReturn(123)
        ctrl.add("test")
        verify(questPresetsDao).add("test")
        verify(listener).onAddedQuestPreset(QuestPreset(123, "test"))
    }

    @Test fun delete() {
        ctrl.delete(123)
        verify(questPresetsDao).delete(123)
        verify(listener).onDeletedQuestPreset(123)
    }

    @Test fun `delete current preset switches to preset 0`() {
        on(ctrl.selectedId).thenReturn(55)
        ctrl.delete(55)
        verify(questPresetsDao).delete(55)
        verify(listener).onDeletedQuestPreset(55)
        verify(prefs).selectedQuestPreset = 0L
    }

    @Test fun `change current preset`() {
        ctrl.selectedId = 11
        verify(prefs).selectedQuestPreset = 11
        verify(prefs).onSelectedQuestPresetChanged(any())
    }
}
