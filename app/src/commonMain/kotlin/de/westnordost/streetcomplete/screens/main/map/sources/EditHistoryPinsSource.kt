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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class EditHistoryPinsSource(
    private val editHistorySource: EditHistorySource
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val pins: Flow<Collection<Pin>> = callbackFlow {
        val listener = object : EditHistorySource.Listener {
            override fun onAdded(added: Edit) { trySend(Unit) }
            override fun onSynced(synced: Edit) { }
            override fun onDeleted(deleted: List<Edit>) { trySend(Unit) }
            override fun onInvalidated() { trySend(Unit) }
        }
        // Subscribe before reading so edits made during the initial load are not lost.
        editHistorySource.addListener(listener)
        trySend(Unit)
        awaitClose { editHistorySource.removeListener(listener) }
    }.buffer(Channel.CONFLATED).mapLatest {
        withContext(Dispatchers.IO) {
            editHistorySource.getAll().mapIndexed { index, edit -> edit.toEditPin(index) }
        }
    }

    fun getEditKey(properties: JsonObject): EditKey? =
        properties.toEditKey()
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

private fun Edit.toEditPin(order: Int) = Pin(position, icon!!, toProperties(), order)

private fun Edit.toProperties(): List<Pair<String, JsonPrimitive>> = when (this) {
    is ElementEdit -> listOf(
        MARKER_EDIT_TYPE to JsonPrimitive(EDIT_TYPE_ELEMENT),
        MARKER_ID to JsonPrimitive(id)
    )
    is NoteEdit -> listOf(
        MARKER_EDIT_TYPE to JsonPrimitive(EDIT_TYPE_NOTE),
        MARKER_ID to JsonPrimitive(id)
    )
    is OsmNoteQuestHidden -> listOf(
        MARKER_EDIT_TYPE to JsonPrimitive(EDIT_TYPE_HIDE_OSM_NOTE_QUEST),
        MARKER_NOTE_ID to JsonPrimitive(note.id)
    )
    is OsmQuestHidden -> listOf(
        MARKER_EDIT_TYPE to JsonPrimitive(EDIT_TYPE_HIDE_OSM_QUEST),
        MARKER_ELEMENT_TYPE to JsonPrimitive(elementType.name),
        MARKER_ELEMENT_ID to JsonPrimitive(elementId),
        MARKER_QUEST_TYPE to JsonPrimitive(questType.name)
    )
    else -> throw IllegalArgumentException()
}

private fun JsonObject.toEditKey(): EditKey? {
    val editType = get(MARKER_EDIT_TYPE)?.jsonPrimitive?.contentOrNull
    return when (editType) {
        EDIT_TYPE_ELEMENT ->
            ElementEditKey(getValue(MARKER_ID).jsonPrimitive.long)
        EDIT_TYPE_NOTE ->
            NoteEditKey(getValue(MARKER_ID).jsonPrimitive.long)
        EDIT_TYPE_HIDE_OSM_QUEST ->
            QuestHiddenKey(OsmQuestKey(
                ElementType.valueOf(getValue(MARKER_ELEMENT_TYPE).jsonPrimitive.content),
                getValue(MARKER_ELEMENT_ID).jsonPrimitive.long,
                getValue(MARKER_QUEST_TYPE).jsonPrimitive.content
            ))
        EDIT_TYPE_HIDE_OSM_NOTE_QUEST ->
            QuestHiddenKey(OsmNoteQuestKey(getValue(MARKER_NOTE_ID).jsonPrimitive.long))
        else -> null
    }
}
