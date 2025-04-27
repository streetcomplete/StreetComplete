package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.presets.EditTypePresetsDao
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EditTypePresetControllerTest {

    private lateinit var editTypePresetsDao: EditTypePresetsDao
    private lateinit var prefs: Preferences
    private lateinit var ctrl: EditTypePresetsController
    private lateinit var listener: EditTypePresetsSource.Listener

    private val preset = EditTypePreset(1, "test")

    @BeforeTest fun setUp() {
        editTypePresetsDao = mock()
        prefs = mock()
        ctrl = EditTypePresetsController(editTypePresetsDao, prefs)

        listener = mock()
        ctrl.addListener(listener)
    }

    @Test fun get() {
        on(editTypePresetsDao.getName(1)).thenReturn("huhu")
        on(prefs.selectedEditTypePreset).thenReturn(1)
        assertEquals("huhu", ctrl.selectedEditTypePresetName)
    }

    @Test fun getAll() {
        on(editTypePresetsDao.getAll()).thenReturn(listOf(preset))
        assertEquals(listOf(preset), ctrl.getAll())
    }

    @Test fun add() {
        on(editTypePresetsDao.add(any())).thenReturn(123)
        ctrl.add("test")
        verify(editTypePresetsDao).add("test")
        verify(listener).onAdded(EditTypePreset(123, "test"))
    }

    @Test fun delete() {
        ctrl.delete(123)
        verify(editTypePresetsDao).delete(123)
        verify(listener).onDeleted(123)
    }

    @Test fun `delete current preset switches to preset 0`() {
        on(ctrl.selectedId).thenReturn(55)
        ctrl.delete(55)
        verify(editTypePresetsDao).delete(55)
        verify(listener).onDeleted(55)
        verify(prefs).selectedEditTypePreset = 0L
    }

    @Test fun `change current preset`() {
        ctrl.selectedId = 11
        verify(prefs).selectedEditTypePreset = 11
        verify(prefs).onSelectedEditTypePresetChanged(any())
    }
}
