package de.westnordost.streetcomplete.map

import android.content.res.Resources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.westnordost.streetcomplete.data.edithistory.*
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.map.components.Pin
import de.westnordost.streetcomplete.map.components.PinsMapComponent
import kotlinx.coroutines.*

class EditHistoryPinsManager(
    private val pinsMapComponent: PinsMapComponent,
    private val editHistorySource: EditHistorySource,
    private val resources: Resources
): LifecycleObserver {

    /** Switch visibility of edit history pins layer */
    var isVisible: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) show() else hide()
        }

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val editHistoryListener = object : EditHistorySource.Listener {
        override fun onAdded(edit: Edit) { updatePins() }
        override fun onSynced(edit: Edit) {}
        override fun onDeleted(edits: List<Edit>) { updatePins() }
        override fun onInvalidated() { updatePins() }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY) fun onDestroy() {
        hide()
        viewLifecycleScope.cancel()
    }

    private fun show() {
        updatePins()
        editHistorySource.addListener(editHistoryListener)
    }

    private fun hide() {
        pinsMapComponent.clear()
        viewLifecycleScope.coroutineContext.cancelChildren()
        editHistorySource.removeListener(editHistoryListener)
    }

    fun getEditKey(properties: Map<String, String>): EditKey? =
        properties.toEditKey()

    private fun updatePins() {
        viewLifecycleScope.launch {
            if (isVisible) {
                val edits = withContext(Dispatchers.IO) { editHistorySource.getAll() }
                pinsMapComponent.showPins(createEditPins(edits))
            }
        }
    }

    private fun createEditPins(edits: List<Edit>): List<Pin> =
        edits.mapIndexed { index, edit ->
            Pin(
                edit.position,
                resources.getResourceEntryName(edit.icon),
                edit.toProperties(),
                edits.size - index // most recent first
            )
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


private fun Edit.toProperties(): Map<String, String> = when(this) {
    is ElementEdit -> mapOf(
        MARKER_EDIT_TYPE to EDIT_TYPE_ELEMENT,
        MARKER_ID to id.toString()
    )
    is NoteEdit -> mapOf(
        MARKER_EDIT_TYPE to EDIT_TYPE_NOTE,
        MARKER_ID to id.toString()
    )
    is OsmNoteQuestHidden -> mapOf(
        MARKER_EDIT_TYPE to EDIT_TYPE_HIDE_OSM_NOTE_QUEST,
        MARKER_NOTE_ID to note.id.toString()
    )
    is OsmQuestHidden -> mapOf(
        MARKER_EDIT_TYPE to EDIT_TYPE_HIDE_OSM_QUEST,
        MARKER_ELEMENT_TYPE to elementType.name,
        MARKER_ELEMENT_ID to elementId.toString(),
        MARKER_QUEST_TYPE to questType::class.simpleName!!
    )
    else -> throw IllegalArgumentException()
}

private fun Map<String, String>.toEditKey(): EditKey? = when(get(MARKER_EDIT_TYPE)) {
    EDIT_TYPE_ELEMENT ->
        ElementEditKey(getValue(MARKER_ID).toLong())
    EDIT_TYPE_NOTE ->
        NoteEditKey(getValue(MARKER_ID).toLong())
    EDIT_TYPE_HIDE_OSM_QUEST ->
        OsmQuestHiddenKey(OsmQuestKey(
            getValue(MARKER_ELEMENT_TYPE).let { ElementType.valueOf(it) },
            getValue(MARKER_ELEMENT_ID).toLong(),
            getValue(MARKER_QUEST_TYPE)
        ))
    EDIT_TYPE_HIDE_OSM_NOTE_QUEST ->
        OsmNoteQuestHiddenKey(OsmNoteQuestKey(getValue(MARKER_NOTE_ID).toLong()))
    else -> null
}
