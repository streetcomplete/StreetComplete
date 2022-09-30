package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.util.SpatialCache
import java.util.concurrent.CopyOnWriteArrayList

/** Access and listen to quests visible on the map */
class VisibleQuestsSource(
    private val questTypeRegistry: QuestTypeRegistry,
    private val osmQuestSource: OsmQuestSource,
    private val osmNoteQuestSource: OsmNoteQuestSource,
    private val visibleQuestTypeSource: VisibleQuestTypeSource,
    private val teamModeQuestFilter: TeamModeQuestFilter,
    private val selectedOverlaySource: SelectedOverlaySource
) {
    interface Listener {
        /** Called when given quests in the given group have been added/removed */
        fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>)
        /** Called when something has changed which should trigger any listeners to update all */
        fun onVisibleQuestsInvalidated()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    private val osmQuestSourceListener = object : OsmQuestSource.Listener {
        override fun onUpdated(addedQuests: Collection<OsmQuest>, deletedQuestKeys: Collection<OsmQuestKey>) {
            updateVisibleQuests(addedQuests.filter(::isVisible), deletedQuestKeys)
        }
        override fun onInvalidated() {
            // apparently the visibility of many different quests have changed
            invalidate()
        }
    }

    private val osmNoteQuestSourceListener = object : OsmNoteQuestSource.Listener {
        override fun onUpdated(addedQuests: Collection<OsmNoteQuest>, deletedQuestIds: Collection<Long>) {
            updateVisibleQuests(addedQuests.filter(::isVisible), deletedQuestIds.map { OsmNoteQuestKey(it) })
        }
        override fun onInvalidated() {
            // apparently the visibility of many different notes have changed
            invalidate()
        }
    }

    private val visibleQuestTypeSourceListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean) {
            // many different quests could become visible/invisible when this is changed
            invalidate()
        }

        override fun onQuestTypeVisibilitiesChanged() {
            // many different quests could become visible/invisible when this is changed
            invalidate()
        }
    }

    private val teamModeQuestFilterListener = object : TeamModeQuestFilter.TeamModeChangeListener {
        override fun onTeamModeChanged(enabled: Boolean) {
            invalidate()
        }
    }

    private val selectedOverlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            invalidate()
        }
    }

    private val cache = SpatialCache(
        SPATIAL_CACHE_TILE_ZOOM,
        SPATIAL_CACHE_TILES,
        SPATIAL_CACHE_INITIAL_CAPACITY,
        { getAllVisibleFromDatabase(it) },
        Quest::key, Quest::position
    )
    init {
        osmQuestSource.addListener(osmQuestSourceListener)
        osmNoteQuestSource.addListener(osmNoteQuestSourceListener)
        visibleQuestTypeSource.addListener(visibleQuestTypeSourceListener)
        teamModeQuestFilter.addListener(teamModeQuestFilterListener)
        selectedOverlaySource.addListener(selectedOverlayListener)
    }

    fun getAllVisible(bbox: BoundingBox): List<Quest> =
        cache.get(bbox)

    /** Retrieve all visible quests in the given bounding box from local database */
    private fun getAllVisibleFromDatabase(bbox: BoundingBox): List<Quest> {
        val visibleQuestTypeNames = questTypeRegistry.filter { isVisible(it) }.map { it.name }
        if (visibleQuestTypeNames.isEmpty()) return listOf()

        val osmQuests = osmQuestSource.getAllVisibleInBBox(bbox, visibleQuestTypeNames)
        val osmNoteQuests = osmNoteQuestSource.getAllVisibleInBBox(bbox)

        return if (teamModeQuestFilter.isEnabled) {
            osmQuests.filter(::isVisibleInTeamMode) + osmNoteQuests.filter(::isVisibleInTeamMode)
        } else {
            osmQuests + osmNoteQuests
        }
    }

    fun get(questKey: QuestKey): Quest? = cache.get(questKey) ?: when (questKey) {
        is OsmNoteQuestKey -> osmNoteQuestSource.get(questKey.noteId)
        is OsmQuestKey -> osmQuestSource.get(questKey)
    }?.takeIf { isVisible(it) }

    private fun isVisible(questType: QuestType): Boolean =
        visibleQuestTypeSource.isVisible(questType) &&
        selectedOverlaySource.selectedOverlay?.let { questType.name !in it.hidesQuestTypes } ?: true

    private fun isVisible(quest: Quest): Boolean =
        isVisibleInTeamMode(quest) && isVisible(quest.type)

    private fun isVisibleInTeamMode(quest: Quest): Boolean =
        teamModeQuestFilter.isVisible(quest)

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun clearCache() = cache.clear()

    fun trimCache() = cache.trim(SPATIAL_CACHE_TILES / 3)

    private fun updateVisibleQuests(addedQuests: Collection<Quest>, deletedQuestKeys: Collection<QuestKey>) {
        if (addedQuests.isEmpty() && deletedQuestKeys.isEmpty()) return
        cache.update(addedQuests, deletedQuestKeys)
        listeners.forEach { it.onUpdatedVisibleQuests(addedQuests, deletedQuestKeys) }
    }

    private fun invalidate() {
        clearCache()
        listeners.forEach { it.onVisibleQuestsInvalidated() }
    }
}

// same tile zoom as used in QuestPinsManager which is the only caller of getAllVisible and only
// ever queries tiles in that zoom
private const val SPATIAL_CACHE_TILE_ZOOM = 16
// set a large number of tiles, as the cache is not large in memory and it allows
// better UX when scrolling the map
private const val SPATIAL_CACHE_TILES = 128
// in a city this is the approximate number of quests in ~30 tiles on default visibilities
private const val SPATIAL_CACHE_INITIAL_CAPACITY = 10000
