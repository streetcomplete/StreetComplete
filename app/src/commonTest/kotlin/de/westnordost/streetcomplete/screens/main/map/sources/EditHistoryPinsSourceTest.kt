package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.ElementEditKey
import de.westnordost.streetcomplete.data.edithistory.NoteEditKey
import de.westnordost.streetcomplete.data.edithistory.QuestHiddenKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.testutils.edit
import de.westnordost.streetcomplete.testutils.noteEdit
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EditHistoryPinsSourceTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun reloadsAndReindexesCompleteHistoryAfterChanges() = runTest {
        var edits: List<Edit> = listOf(noteEdit(id = 1), edit(id = 2))
        lateinit var listener: EditHistorySource.Listener
        val history: EditHistorySource = mock {
            every { getAll() } calls { edits }
            every { addListener(any()) } calls { (value: EditHistorySource.Listener) ->
                listener = value
            }
        }
        val source = EditHistoryPinsSource(
            history,
            workerDispatcher = StandardTestDispatcher(testScheduler),
        )

        advanceUntilIdle()
        assertEquals(emptyList(), source.pins.value.pins)

        source.setActive(true)
        advanceUntilIdle()
        assertEquals(listOf(0, 1), source.pins.value.pins.map { it.order })
        assertEquals(listOf(NoteEditKey(1), ElementEditKey(2)), source.pins.value.pins.map {
            source.getEditKey(it.properties.toMap())
        })

        edits = listOf(edits.last())
        listener.onDeleted(listOf(noteEdit(id = 1)))
        advanceUntilIdle()

        assertEquals(listOf(0), source.pins.value.pins.map { it.order })
        assertEquals(
            ElementEditKey(2),
            source.getEditKey(source.pins.value.pins.single().properties.toMap()),
        )

        source.setActive(false)
        advanceUntilIdle()
        assertEquals(emptyList(), source.pins.value.pins)

        edits = listOf(noteEdit(id = 3))
        listener.onAdded(edits.single())
        advanceUntilIdle()
        assertEquals(emptyList(), source.pins.value.pins)
        source.close()
    }

    @Test fun hiddenQuestKeysDecodeFromMapProperties() {
        assertEquals(
            QuestHiddenKey(OsmNoteQuestKey(3)),
            mapOf("edit_type" to "hide_osm_note_quest", "note_id" to "3").toEditKeyOrNull(),
        )
        assertEquals(
            QuestHiddenKey(OsmQuestKey(ElementType.RELATION, 4, "CheckExistence")),
            mapOf(
                "edit_type" to "hide_osm_quest",
                "element_type" to "RELATION",
                "element_id" to "4",
                "quest_type" to "CheckExistence",
            ).toEditKeyOrNull(),
        )
    }

    @Test fun malformedEditPropertiesAreIgnored() {
        assertNull(mapOf("edit_type" to "note", "id" to "oops").toEditKeyOrNull())
        assertNull(mapOf("edit_type" to "hide_osm_quest").toEditKeyOrNull())
        assertNull(emptyMap<String, String>().toEditKeyOrNull())
    }
}
