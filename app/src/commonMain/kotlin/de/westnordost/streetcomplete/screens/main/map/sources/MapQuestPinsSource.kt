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
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

class MapQuestPinsSource(
    private val questTypeOrderSource: QuestTypeOrderSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestsSource: VisibleQuestsSource
) {
    private val displayedRect = MutableStateFlow<TilesRect?>(null)

    // Only candidate keys survive inactivity; their current visibility and data are re-read.
    private var multiMarkerQuestKeys = emptySet<QuestKey>()

    val pins: Flow<Collection<Pin>> = channelFlow {
        val questsInView = mutableMapOf<QuestKey, List<Pin>>()
        displayedRect.collectLatest { rect ->
            if (rect == null) {
                questsInView.clear()
                send(emptyList())
                return@collectLatest
            }
            val bbox = rect.asBoundingBox(TILES_ZOOM)
            var orders = emptyMap<QuestType, Int>()
            events().collect { event ->
                when (event) {
                    Event.Reload -> {
                        val (quests, questOrders) = withContext(Dispatchers.IO) {
                            val types = questTypeRegistry.toMutableList()
                            questTypeOrderSource.sort(types)
                            // A long quest can have a visible marker but its center outside this view.
                            val retained = multiMarkerQuestKeys.mapNotNull { visibleQuestsSource.get(it) }
                                .filter { quest -> quest.markerLocations.any { it in bbox } }
                            (retained + visibleQuestsSource.getAll(bbox)) to
                                types.withIndex().associate { it.value to it.index }
                        }
                        orders = questOrders
                        questsInView.clear()
                        quests.forEach { questsInView[it.key] = it.toPins(orders) }
                    }
                    is Event.Updated -> {
                        event.removed.forEach { questsInView.remove(it) }
                        event.added.forEach { quest ->
                            if (quest.markerLocations.any { it in bbox }) {
                                questsInView[quest.key] = quest.toPins(orders)
                            } else {
                                questsInView.remove(quest.key)
                            }
                        }
                    }
                }
                multiMarkerQuestKeys = questsInView.filterValues { it.size > 1 }.keys.toSet()
                send(questsInView.values.flatten())
            }
        }
    }.flowOn(Dispatchers.Default)

    // Callbacks may arrive on different threads; only the collector mutates the displayed data.
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

    private fun Quest.toPins(orders: Map<QuestType, Int>): List<Pin> =
        markerLocations.map { Pin(it, type.icon, key.toProperties(), orders[type] ?: 0) }

    private sealed interface Event {
        data object Reload : Event
        data class Updated(val added: List<Quest>, val removed: List<QuestKey>) : Event
    }

    companion object {
        private const val TILES_ZOOM = 16
    }
}


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
