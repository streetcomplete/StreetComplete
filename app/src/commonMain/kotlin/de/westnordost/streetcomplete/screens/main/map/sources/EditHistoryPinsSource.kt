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
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/** Source for edit history [pins] on the map. Unlike in the [MapQuestPinsSource], this [pins] flow
 *  contains pins of **all** edits still in the database. This is because we don't expect there to
 *  be potentially many thousands of edits */
class EditHistoryPinsSource(
    private val editHistorySource: EditHistorySource
) {
    val pins: Flow<Collection<Pin>> = flow {
        val pinsByKey = mutableMapOf<EditKey, Pin>()
        emitAll(events().map { event ->
            when (event) {
                is Event.Added -> {
                    val edit = event.edit
                    val order = pinsByKey[edit.key]?.order ?: pinsByKey.size
                    pinsByKey[edit.key] = edit.toEditPin(order)
                }
                is Event.Deleted -> {
                    event.keys.forEach { pinsByKey.remove(it) }
                }
                Event.Invalidated -> {
                    val edits = withContext(Dispatchers.IO) { editHistorySource.getAll() }
                    pinsByKey.clear()
                    for ((index, edit) in edits.withIndex()) {
                        pinsByKey[edit.key] = edit.toEditPin(index)
                    }
                }
            }
            pinsByKey.values.toList()
        })
    }

    private sealed interface Event {
        data class Added(val edit: Edit) : Event
        data class Deleted(val keys: List<EditKey>) : Event
        data object Invalidated : Event
    }

    private fun events(): Flow<Event> = callbackFlow {
        val listener = object : EditHistorySource.Listener {
            override fun onAdded(added: Edit) { trySend(Event.Added(added)) }
            override fun onSynced(synced: Edit) { }
            override fun onDeleted(deleted: List<Edit>) {
                trySend(Event.Deleted(deleted.map { it.key }))
            }
            override fun onInvalidated() { trySend(Event.Invalidated) }
        }
        editHistorySource.addListener(listener)
        trySend(Event.Invalidated)
        awaitClose {
            editHistorySource.removeListener(listener)
        }
    }.buffer(Channel.UNLIMITED)

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
