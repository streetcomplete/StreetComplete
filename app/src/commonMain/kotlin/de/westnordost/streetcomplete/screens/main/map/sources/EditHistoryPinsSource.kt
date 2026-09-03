package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.edithistory.ElementEditKey
import de.westnordost.streetcomplete.data.edithistory.NoteEditKey
import de.westnordost.streetcomplete.data.edithistory.QuestHiddenKey
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.screens.main.edithistory.icon
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.layers.PinSnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Renderer-independent edit-history pins, ordered exactly as the history source returns them. */
class EditHistoryPinsSource(
    private val editHistorySource: EditHistorySource,
    workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private val _pins = MutableStateFlow(PinSnapshot.Empty)
    val pins: StateFlow<PinSnapshot> = _pins.asStateFlow()

    private var reloadJob: Job? = null
    private var isClosed = false

    private val listener = object : EditHistorySource.Listener {
        override fun onAdded(added: Edit) = reload()
        override fun onSynced(synced: Edit) = Unit
        override fun onDeleted(deleted: List<Edit>) = reload()
        override fun onInvalidated() = reload()
    }

    init {
        editHistorySource.addListener(listener)
        reload()
    }

    fun getEditKey(properties: Map<String, String>): EditKey? =
        properties.toEditKeyOrNull()

    fun close() {
        if (isClosed) return
        isClosed = true
        editHistorySource.removeListener(listener)
        scope.cancel()
    }

    private fun reload() {
        if (isClosed) return
        reloadJob?.cancel()
        reloadJob = scope.launch {
            val pins = editHistorySource.getAll().mapIndexed { index, edit ->
                Pin(
                    position = edit.position,
                    icon = requireNotNull(edit.icon) { "Unsupported edit type ${edit::class.simpleName}" },
                    properties = edit.editProperties(),
                    order = index,
                )
            }
            _pins.update { it.updated(pins) }
        }
    }
}

private const val MARKER_EDIT_TYPE = "edit_type"
private const val MARKER_ELEMENT_TYPE = "element_type"
private const val MARKER_ELEMENT_ID = "element_id"
private const val MARKER_QUEST_TYPE = "quest_type"
private const val MARKER_NOTE_ID = "note_id"
private const val MARKER_ID = "id"
private const val EDIT_TYPE_ELEMENT = "element"
private const val EDIT_TYPE_NOTE = "note"
private const val EDIT_TYPE_HIDE_OSM_NOTE_QUEST = "hide_osm_note_quest"
private const val EDIT_TYPE_HIDE_OSM_QUEST = "hide_osm_quest"

internal fun Edit.editProperties(): List<Pair<String, String>> = when (this) {
    is ElementEdit -> listOf(
        MARKER_EDIT_TYPE to EDIT_TYPE_ELEMENT,
        MARKER_ID to id.toString(),
    )
    is NoteEdit -> listOf(
        MARKER_EDIT_TYPE to EDIT_TYPE_NOTE,
        MARKER_ID to id.toString(),
    )
    is OsmNoteQuestHidden -> listOf(
        MARKER_EDIT_TYPE to EDIT_TYPE_HIDE_OSM_NOTE_QUEST,
        MARKER_NOTE_ID to note.id.toString(),
    )
    is OsmQuestHidden -> listOf(
        MARKER_EDIT_TYPE to EDIT_TYPE_HIDE_OSM_QUEST,
        MARKER_ELEMENT_TYPE to elementType.name,
        MARKER_ELEMENT_ID to elementId.toString(),
        MARKER_QUEST_TYPE to questType.name,
    )
    else -> error("Unsupported edit type ${this::class.simpleName}")
}

internal fun Map<String, String>.toEditKeyOrNull(): EditKey? = when (get(MARKER_EDIT_TYPE)) {
    EDIT_TYPE_ELEMENT -> get(MARKER_ID)?.toLongOrNull()?.let(::ElementEditKey)
    EDIT_TYPE_NOTE -> get(MARKER_ID)?.toLongOrNull()?.let(::NoteEditKey)
    EDIT_TYPE_HIDE_OSM_NOTE_QUEST -> get(MARKER_NOTE_ID)?.toLongOrNull()?.let {
        QuestHiddenKey(OsmNoteQuestKey(it))
    }
    EDIT_TYPE_HIDE_OSM_QUEST -> {
        val elementType = get(MARKER_ELEMENT_TYPE)?.let {
            ElementType.entries.firstOrNull { type -> type.name == it }
        }
        val elementId = get(MARKER_ELEMENT_ID)?.toLongOrNull()
        val questType = get(MARKER_QUEST_TYPE)
        if (elementType != null && elementId != null && questType != null) {
            QuestHiddenKey(OsmQuestKey(elementType, elementId, questType))
        } else {
            null
        }
    }
    else -> null
}
