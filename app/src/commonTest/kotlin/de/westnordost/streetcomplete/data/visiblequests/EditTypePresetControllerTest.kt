package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.presets.EditTypePresetsDao
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.verify
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
        every { editTypePresetsDao.getName(1) } returns "huhu"
        every { prefs.selectedEditTypePreset } returns 1
        assertEquals("huhu", ctrl.selectedEditTypePresetName)
    }

    @Test fun getAll() {
        every { editTypePresetsDao.getAll() } returns listOf(preset)
        assertEquals(listOf(preset), ctrl.getAll())
    }

    @Test fun add() {
        every { editTypePresetsDao.add(any()) } returns 123
        ctrl.add("test")
        verify { editTypePresetsDao.add("test") }
        verify { listener.onAdded(EditTypePreset(123, "test")) }
    }

    @Test fun delete() {
        ctrl.delete(123)
        verify { editTypePresetsDao.delete(123) }
        verify { listener.onDeleted(123) }
    }

    @Test fun `delete current preset switches to preset 0`() {
        every { ctrl.selectedId } returns 55
        ctrl.delete(55)
        verify { editTypePresetsDao.delete(55) }
        verify { listener.onDeleted(55) }
        verify { prefs.selectedEditTypePreset = 0L }    }

    @Test fun `change current preset`() {
        ctrl.selectedId = 11
        verify { prefs.selectedEditTypePreset = 11 }
        verify { prefs.onSelectedEditTypePresetChanged(any()) }
    }
}
