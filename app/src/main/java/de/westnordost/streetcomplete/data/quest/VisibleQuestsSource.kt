package de.westnordost.streetcomplete.data.quest

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.visiblequests.DayNightQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeSource
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.SpatialCache

/**
 *  Access and listen to quests visible on the map.
 *
 *  This class unifies quests from all sources, i.e. currently OpenStreetMap quests
 *  (see [OsmQuestSource]) and OpenStreetMap note quests (see [OsmNoteQuestSource]).
 *
 *  Quests can be not visible for a user for the following reasons:
 *  - when the user has hidden a quest, see [QuestsHiddenSource]
 *  - when the type of the quest is disabled in the user settings, see [VisibleEditTypeSource]
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
    private val visibleEditTypeSource: VisibleEditTypeSource,
    private val teamModeQuestFilter: TeamModeQuestFilter,
    private val selectedOverlaySource: SelectedOverlaySource,
    private val levelFilter: LevelFilter,
    private val dayNightQuestFilter: DayNightQuestFilter,
    private val prefs: ObservableSettings,
    private val externalSourceQuestController: ExternalSourceQuestController
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
                is ExternalSourceQuestKey -> externalSourceQuestController.get(key)
            } ?: return
            updateVisibleQuests(added = listOf(quest))
        }

        override fun onUnhidAll() {
            // many quests may have been un-hidden
            invalidate()
        }
    }

    private val visibleEditTypeSourceListener = object : VisibleEditTypeSource.Listener {
        override fun onVisibilityChanged(editType: EditType, visible: Boolean) {
            // many different quests could become visible/invisible when this is changed
            if (editType is QuestType) invalidate()
        }

        override fun onVisibilitiesChanged() {
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
            // no need to invalidate if overlay can't hide quests
            if (prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true))
                invalidate()
        }
    }

    private val otherQuestListener = object : ExternalSourceQuestController.QuestListener {
        override fun onUpdated(addedQuests: Collection<ExternalSourceQuest>, deletedQuestKeys: Collection<ExternalSourceQuestKey>) {
            val hideOverlayQuests = prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true)
            updateVisibleQuests(addedQuests.filter { isVisible(it, hideOverlayQuests) }, deletedQuestKeys)
        }
        override fun onInvalidate() = invalidate()
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
        visibleEditTypeSource.addListener(visibleEditTypeSourceListener)
        teamModeQuestFilter.addListener(teamModeQuestFilterListener)
        selectedOverlaySource.addListener(selectedOverlayListener)
        externalSourceQuestController.addQuestListener(otherQuestListener)
    }

    fun getAll(bbox: BoundingBox): List<Quest> =
        cache.get(bbox)

    /** Retrieve all visible quests in the given bounding box from local database */
    private fun getAllFromDatabase(bbox: BoundingBox): List<Quest> {
        // we could just get all quests from the quest sources and then filter it with
        // isVisible(quest) but we can optimize here by querying only quests of types that are
        // currently visible
        val hideOverlayQuests = prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true)
        val visibleQuestTypes = questTypeRegistry.filter { isVisible(it, hideOverlayQuests) }
        println(hideOverlayQuests)
        println(visibleQuestTypes)
        if (visibleQuestTypes.isEmpty()) return emptyList()

        val quests =
            osmQuestSource.getAllInBBox(bbox, visibleQuestTypes) +
            osmNoteQuestSource.getAllInBBox(bbox) +
            externalSourceQuestController.getAllInBBox(bbox, visibleQuestTypes)

        println(quests)
        return quests.filter { isVisible(it.key) && isVisibleInTeamMode(it) }
    }

    fun get(questKey: QuestKey): Quest? {
        val quest = cache.get(questKey) ?: when (questKey) {
            is OsmNoteQuestKey -> osmNoteQuestSource.get(questKey.noteId)
            is OsmQuestKey -> osmQuestSource.get(questKey)
            is ExternalSourceQuestKey -> externalSourceQuestController.get(questKey)
        } ?: return null
        return if (isVisible(quest, prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true))) quest else null
    }

    private fun isVisible(quest: Quest, hideOverlayQuests: Boolean): Boolean =
        isVisible(quest.key) && isVisibleInTeamMode(quest) && isVisible(quest.type, hideOverlayQuests)

    private fun isVisible(questType: QuestType, hideOverlayQuests: Boolean): Boolean =
        visibleEditTypeSource.isVisible(questType) &&
        selectedOverlaySource.selectedOverlay?.let { !hideOverlayQuests || questType.name !in it.hidesQuestTypes } ?: true

    private fun isVisible(questKey: QuestKey): Boolean =
        questsHiddenSource.get(questKey) == null

    private fun isVisibleInTeamMode(quest: Quest): Boolean =
        teamModeQuestFilter.isVisible(quest) && levelFilter.isVisible(quest) && dayNightQuestFilter.isVisible(quest)

    fun getNearbyQuests(quest: Quest, distance: Double): Collection<Quest> {
        val bbox = quest.position.enclosingBoundingBox(distance)
        return when (prefs.getInt(Prefs.SHOW_NEARBY_QUESTS, 0)) {
            1 -> getAll(bbox)
            2 -> (osmQuestSource.getAllInBBox(bbox) +
                    externalSourceQuestController.getAllInBBox(bbox) +
                    osmNoteQuestSource.getAllInBBox(bbox)
                ).filter { isVisible(it.key) && isVisibleInTeamMode(it) }
            3 -> (osmQuestSource.getAllInBBox(bbox) +
                    externalSourceQuestController.getAllInBBox(bbox) +
                    osmNoteQuestSource.getAllInBBox(bbox)
                ).filter { isVisibleInTeamMode(it) }
            else -> emptyList()
        }
    }

    fun clearCachedQuestPins() {
        cache.getItems().forEach { it.pins = null }
    }

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
            val hideOverlayQuests = prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true)
            val addedVisible = added.filter { isVisible(it, hideOverlayQuests) }
            if (addedVisible.isEmpty() && deleted.isEmpty()) return

            if (addedVisible.size > 10 || deleted.size > 10) Log.i(TAG, "added ${addedVisible.size}, deleted ${deleted.size}")
            else Log.i(TAG, "added ${addedVisible.map { it.key }}, deleted: $deleted")

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

private const val TAG = "VisibleQuestsSource"
