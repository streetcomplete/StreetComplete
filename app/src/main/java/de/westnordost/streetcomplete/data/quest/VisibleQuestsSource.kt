package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.SpatialCache

/**
 *  Access and listen to quests visible on the map.
 *
 *  This class unifies quests from all sources, i.e. currently OpenStreetMap quests
 *  (see [OsmQuestSource]) and OpenStreetMap note quests (see [OsmNoteQuestSource]).
 *
 *  Quests can be not visible for a user for the following reasons:
 *  - when the user has hidden a quest, see [QuestsHiddenSource]
 *  - when the type of the quest is disabled in the user settings, see [VisibleQuestTypeSource]
 *  - when the team mode is activated, only every Xth quest is visible, see [TeamModeQuestFilter]
 *  - when the selected overlay disables the quest type because the overlay lets the user edit
 *   the same info as the quest, see [SelectedOverlaySource] / [Overlay.hidesQuestTypes][de.westnordost.streetcomplete.overlays.Overlay.hidesQuestTypes]
 *
 *  Note that quests can also be not visible due to source-specific reasons, this is managed
 *  by the individual sources of quests. (E.g. osm quests at an osm note are not visible, osm note
 *  quests are not visible by default if they are not phrased as questions, …)
 *  */
class VisibleQuestsSource(
    private val questTypeRegistry: QuestTypeRegistry,
    private val osmQuestSource: OsmQuestSource,
    private val osmNoteQuestSource: OsmNoteQuestSource,
    private val questsHiddenSource: QuestsHiddenSource,
    private val visibleQuestTypeSource: VisibleQuestTypeSource,
    private val teamModeQuestFilter: TeamModeQuestFilter,
    private val selectedOverlaySource: SelectedOverlaySource
) {
    interface Listener {
        /** Called when given quests in the given group have been added/removed */
        fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>)
        /** Called when something has changed which should trigger any listeners to update all */
        fun onInvalidated()
    }

    private val listeners = Listeners<Listener>()

    private val osmQuestSourceListener = object : OsmQuestSource.Listener {
        override fun onUpdated(added: Collection<OsmQuest>, deleted: Collection<OsmQuestKey>) {
            updateVisibleQuests(added, deleted)
        }
        override fun onInvalidated() {
            // apparently the visibility of many different quests have changed
            invalidate()
        }
    }

    private val osmNoteQuestSourceListener = object : OsmNoteQuestSource.Listener {
        override fun onUpdated(added: Collection<OsmNoteQuest>, deleted: Collection<Long>) {
            updateVisibleQuests(added, deleted.map { OsmNoteQuestKey(it) })
        }
        override fun onInvalidated() {
            // apparently the visibility of many different notes have changed
            invalidate()
        }
    }

    private val questsHiddenSourceListener = object : QuestsHiddenSource.Listener {
        override fun onHid(key: QuestKey, timestamp: Long) {
            updateVisibleQuests(deleted = listOf(key))
        }

        override fun onUnhid(key: QuestKey, timestamp: Long) {
            val quest = when (key) {
                is OsmQuestKey -> osmQuestSource.get(key)
                is OsmNoteQuestKey -> osmNoteQuestSource.get(key.noteId)
            } ?: return
            updateVisibleQuests(added = listOf(quest))
        }

        override fun onUnhidAll() {
            // many quests may have been un-hidden
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
        { getAllFromDatabase(it) },
        Quest::key, Quest::position
    )
    init {
        osmQuestSource.addListener(osmQuestSourceListener)
        osmNoteQuestSource.addListener(osmNoteQuestSourceListener)
        questsHiddenSource.addListener(questsHiddenSourceListener)
        visibleQuestTypeSource.addListener(visibleQuestTypeSourceListener)
        teamModeQuestFilter.addListener(teamModeQuestFilterListener)
        selectedOverlaySource.addListener(selectedOverlayListener)
    }

    fun getAll(bbox: BoundingBox): List<Quest> =
        cache.get(bbox)

    /** Retrieve all visible quests in the given bounding box from local database */
    private fun getAllFromDatabase(bbox: BoundingBox): List<Quest> {
        // we could just get all quests from the quest sources and then filter it with
        // isVisible(quest) but we can optimize here by querying only quests of types that are
        // currently visible
        val visibleQuestTypeNames = questTypeRegistry.filter { isVisible(it) }.map { it.name }
        if (visibleQuestTypeNames.isEmpty()) return listOf()

        val quests =
            osmQuestSource.getAllInBBox(bbox, visibleQuestTypeNames) +
            osmNoteQuestSource.getAllInBBox(bbox)

        return quests.filter { isVisible(it.key) && isVisibleInTeamMode(it) }
    }

    fun get(questKey: QuestKey): Quest? {
        val quest = cache.get(questKey) ?: when (questKey) {
            is OsmNoteQuestKey -> osmNoteQuestSource.get(questKey.noteId)
            is OsmQuestKey -> osmQuestSource.get(questKey)
        } ?: return null
        return if (isVisible(quest)) quest else null
    }

    private fun isVisible(quest: Quest): Boolean =
        isVisible(quest.key) && isVisibleInTeamMode(quest) && isVisible(quest.type)

    private fun isVisible(questType: QuestType): Boolean =
        visibleQuestTypeSource.isVisible(questType) &&
        selectedOverlaySource.selectedOverlay?.let { questType.name !in it.hidesQuestTypes } ?: true

    private fun isVisible(questKey: QuestKey): Boolean =
        questsHiddenSource.get(questKey) == null

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

    private fun updateVisibleQuests(
        added: Collection<Quest> = emptyList(),
        deleted: Collection<QuestKey> = emptyList()
    ) {
        synchronized(this) {
            val addedVisible = added.filter(::isVisible)
            if (addedVisible.isEmpty() && deleted.isEmpty()) return

            cache.update(addedVisible, deleted)
            listeners.forEach { it.onUpdated(addedVisible, deleted) }
        }
    }

    private fun invalidate() {
        synchronized(this) {
            clearCache()
            listeners.forEach { it.onInvalidated() }
        }
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
