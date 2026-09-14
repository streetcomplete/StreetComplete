package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.mutableStateOf
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditItem
import de.westnordost.streetcomplete.testutils.awaitSnapshot
import de.westnordost.streetcomplete.testutils.observing
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.pGeom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditHistoryStateTest {

    private val editItems = MutableStateFlow<List<EditItem>?>(null)
    private val viewModel = object : EditHistoryViewModel() {
        override val editItems: StateFlow<List<EditItem>?> get() = this@EditHistoryStateTest.editItems
        override suspend fun getEditElement(edit: Edit): Element? = null
        override suspend fun getEditGeometry(edit: Edit): ElementGeometry = (edit as ElementEdit).originalGeometry
        override fun undo(editKey: EditKey) {}
    }
    private val state = EditHistoryState(viewModel, mutableStateOf(false), mutableStateOf(null))

    private val edit1 = edit(id = 1, geometry = pGeom(1.0, 1.0))
    private val edit2 = edit(id = 2, geometry = pGeom(2.0, 2.0))

    @Test fun `shows the sidebar with the most recent edit selected and highlighted`() = observing(state::observe) {
        editItems.value = items(edit1, edit2)
        awaitSnapshot { state.editItems != null }

        state.show()
        assertTrue(state.isShowing)
        assertEquals(edit2, state.selectedEdit)
        awaitSnapshot { state.highlightedGeometry == edit2.originalGeometry }
    }

    @Test fun `deselects an edit that was undone and hides once none are left`() = observing(state::observe) {
        editItems.value = items(edit1, edit2)
        awaitSnapshot { state.editItems != null }
        state.show()
        state.select(edit1.key)

        editItems.value = items(edit2)
        awaitSnapshot { state.selectedEditKey == null }
        assertTrue(state.isShowing)
        awaitSnapshot { state.highlightedGeometry == null }

        editItems.value = items()
        awaitSnapshot { !state.isShowing }
    }

    @Test fun `hiding clears the selection`() = observing(state::observe) {
        editItems.value = items(edit1)
        awaitSnapshot { state.editItems != null }
        state.show()
        state.hide()
        assertFalse(state.isShowing)
        assertNull(state.selectedEditKey)
        assertNull(state.selectedEdit)
    }

    private fun items(vararg edits: Edit) = edits.map { EditItem(it, showDate = false, showTime = false) }

}
