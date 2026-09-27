package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.util.math.contains
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/** Source for map quest [pins] on the map. Since there can be a very, very large number of quest
 *  pins on the map, we only show those that are in view. This requires users to call [onMapMoved]
 *  so that the [pins] are updated when the viewport moves to a new area. */
@OptIn(ExperimentalCoroutinesApi::class)
class MapQuestPinsSource(
    private val questTypeOrderSource: QuestTypeOrderSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestsSource: VisibleQuestsSource
) {
    private val displayedRect = MutableStateFlow<TilesRect?>(null)

    val pins: Flow<Collection<Pin>> = flow {
        val pinsByQuest = mutableMapOf<QuestKey, List<Pin>>()
        var orders = emptyMap<QuestType, Int>()

        emitAll(displayedRect.flatMapLatest { rect ->
            if (rect == null) return@flatMapLatest flowOf(emptyList())
            val bbox = rect.asBoundingBox(TILES_ZOOM)
            events().map { event ->
                when (event) {
                    Event.Reload -> {
                        val types = questTypeRegistry.toMutableList()
                        withContext(Dispatchers.IO) { questTypeOrderSource.sort(types) }
                        orders = types.withIndex().associate { it.value to it.index }

                        /* Usually, we would call pinsByQuest.clear() here. However,
                           quests have only a single position, but may have multiple pins (see
                           Quest::markerLocations), e.g. at the start and end of a long road. A pin
                           of a quest whose center is outside the current view may hence be within
                           the current view. Quest pins like these should not disappear when panning
                           the map. Therefore, remove all quests that are not in view anymore that
                           ... (#5802)
                          */
                        pinsByQuest.entries.removeAll { (key, pins) ->
                            // only have one pin (pin position = quest position)
                            pins.size == 1
                            // or has no pins in the current view
                            || pins.none { it.position in bbox }
                        }
                        val quests = withContext(Dispatchers.IO) { visibleQuestsSource.getAll(bbox) }
                        quests.forEach { pinsByQuest[it.key] = it.toPins(orders) }
                    }
                    is Event.Updated -> {
                        event.removed.forEach { pinsByQuest.remove(it) }
                        for (quest in event.added) {
                            if (quest.markerLocations.any { it in bbox }) {
                                pinsByQuest[quest.key] = quest.toPins(orders)
                            } else {
                                pinsByQuest.remove(quest.key)
                            }
                        }
                    }
                }
                pinsByQuest.values.flatten()
            }
        })
    }.flowOn(Dispatchers.Default)



    // Callbacks may arrive on different threads; only the collector mutates the displayed data.
    private sealed interface Event {
        data object Reload : Event
        data class Updated(val added: List<Quest>, val removed: List<QuestKey>) : Event
    }

    private fun events(): Flow<Event> = callbackFlow {
        val questsListener = object : VisibleQuestsSource.Listener {
            override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
                trySend(Event.Updated(added.toList(), removed.toList()))
            }
            override fun onInvalidated() { trySend(Event.Reload) }
        }
        val orderListener = object : QuestTypeOrderSource.Listener {
            override fun onQuestTypeOrderAdded(item: QuestType, toAfter: QuestType) {
                trySend(Event.Reload)
            }
            override fun onQuestTypeOrdersChanged() { trySend(Event.Reload) }
        }
        visibleQuestsSource.addListener(questsListener)
        questTypeOrderSource.addListener(orderListener)
        trySend(Event.Reload)
        awaitClose {
            visibleQuestsSource.removeListener(questsListener)
            questTypeOrderSource.removeListener(orderListener)
        }
    }.buffer(Channel.UNLIMITED)

    fun onMapMoved(zoom: Double, displayedArea: BoundingBox?) {
        if (displayedArea == null) {
            displayedRect.value = null
            return
        }
        // Keep the loaded data when zooming out, including clusters at zoom 13–14.
        if (zoom < 14) return
        val rect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        if (rect.size > 32) return
        if (displayedRect.value?.contains(rect) != true) displayedRect.value = rect
    }

    fun getQuestKey(properties: JsonObject): QuestKey? = properties.toQuestKey()

    companion object {
        private const val TILES_ZOOM = 16
    }
}

private fun Quest.toPins(orders: Map<QuestType, Int>): List<Pin> =
    markerLocations.map { Pin(it, type.icon, key.toProperties(), orders[type] ?: 0) }

private const val MARKER_QUEST_GROUP = "quest_group"

private const val MARKER_ELEMENT_TYPE = "element_type"
private const val MARKER_ELEMENT_ID = "element_id"
private const val MARKER_QUEST_TYPE = "quest_type"
private const val MARKER_NOTE_ID = "note_id"

private const val QUEST_GROUP_OSM = "osm"
private const val QUEST_GROUP_OSM_NOTE = "osm_note"

private fun QuestKey.toProperties(): List<Pair<String, JsonPrimitive>> = when (this) {
    is OsmNoteQuestKey -> listOf(
        MARKER_QUEST_GROUP to JsonPrimitive(QUEST_GROUP_OSM_NOTE),
        MARKER_NOTE_ID to JsonPrimitive(noteId)
    )
    is OsmQuestKey -> listOf(
        MARKER_QUEST_GROUP to JsonPrimitive(QUEST_GROUP_OSM),
        MARKER_ELEMENT_TYPE to JsonPrimitive(elementType.name),
        MARKER_ELEMENT_ID to JsonPrimitive(elementId),
        MARKER_QUEST_TYPE to JsonPrimitive(questTypeName)
    )
}

private fun JsonObject.toQuestKey(): QuestKey? {
    val questGroup = get(MARKER_QUEST_GROUP)?.jsonPrimitive?.contentOrNull
    return when (questGroup) {
        QUEST_GROUP_OSM_NOTE ->
            OsmNoteQuestKey(getValue(MARKER_NOTE_ID).jsonPrimitive.long)
        QUEST_GROUP_OSM ->
            OsmQuestKey(
                ElementType.valueOf(getValue(MARKER_ELEMENT_TYPE).jsonPrimitive.content),
                getValue(MARKER_ELEMENT_ID).jsonPrimitive.long,
                getValue(MARKER_QUEST_TYPE).jsonPrimitive.content
            )
        else -> null
    }
}
