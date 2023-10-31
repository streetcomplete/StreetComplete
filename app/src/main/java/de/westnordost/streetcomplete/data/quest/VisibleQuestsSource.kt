package de.westnordost.streetcomplete.data.quest

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
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
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.util.Log
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.SpatialCache

/** Access and listen to quests visible on the map */
class VisibleQuestsSource(
    private val questTypeRegistry: QuestTypeRegistry,
    private val osmQuestSource: OsmQuestSource,
    private val osmNoteQuestSource: OsmNoteQuestSource,
    private val visibleQuestTypeSource: VisibleQuestTypeSource,
    private val teamModeQuestFilter: TeamModeQuestFilter,
    private val selectedOverlaySource: SelectedOverlaySource,
    private val levelFilter: LevelFilter,
    private val dayNightQuestFilter: DayNightQuestFilter,
    private val prefs: SharedPreferences,
    private val externalSourceQuestController: ExternalSourceQuestController
) {
    interface Listener {
        /** Called when given quests in the given group have been added/removed */
        fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>)
        /** Called when something has changed which should trigger any listeners to update all */
        fun onVisibleQuestsInvalidated()
    }

    private val listeners = Listeners<Listener>()

    private val osmQuestSourceListener = object : OsmQuestSource.Listener {
        override fun onUpdated(addedQuests: Collection<OsmQuest>, deletedQuestKeys: Collection<OsmQuestKey>) {
            val hideOverlayQuests = prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true)
            updateVisibleQuests(addedQuests.filter { isVisible(it, hideOverlayQuests) }, deletedQuestKeys)
        }
        override fun onInvalidated() {
            // apparently the visibility of many different quests have changed
            invalidate()
        }
    }

    private val osmNoteQuestSourceListener = object : OsmNoteQuestSource.Listener {
        override fun onUpdated(addedQuests: Collection<OsmNoteQuest>, deletedQuestIds: Collection<Long>) {
            val hideOverlayQuests = prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true)
            updateVisibleQuests(addedQuests.filter { isVisible(it, hideOverlayQuests) }, deletedQuestIds.map { OsmNoteQuestKey(it) })
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
        { getAllVisibleFromDatabase(it) },
        Quest::key, Quest::position
    )
    init {
        osmQuestSource.addListener(osmQuestSourceListener)
        osmNoteQuestSource.addListener(osmNoteQuestSourceListener)
        visibleQuestTypeSource.addListener(visibleQuestTypeSourceListener)
        teamModeQuestFilter.addListener(teamModeQuestFilterListener)
        selectedOverlaySource.addListener(selectedOverlayListener)
        externalSourceQuestController.addQuestListener(otherQuestListener)
    }

    fun getAllVisible(bbox: BoundingBox): List<Quest> =
        cache.get(bbox)

    /** Retrieve all visible quests in the given bounding box from local database */
    private fun getAllVisibleFromDatabase(bbox: BoundingBox): List<Quest> {
        val hideOverlayQuests = prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true)
        val visibleQuestTypes = questTypeRegistry.filter { isVisible(it, hideOverlayQuests) }
        if (visibleQuestTypes.isEmpty()) return emptyList()

        val osmQuests = osmQuestSource.getAllVisibleInBBox(bbox, visibleQuestTypes)
        val osmNoteQuests = if (visibleQuestTypes.none { it is OsmNoteQuestType }) emptyList()
            else osmNoteQuestSource.getAllVisibleInBBox(bbox)
        val externalSourceQuests = externalSourceQuestController.getAllInBBox(bbox, visibleQuestTypes)

        return if (teamModeQuestFilter.isEnabled || levelFilter.isEnabled || dayNightQuestFilter.isEnabled) {
            osmQuests.filter(::isVisibleInTeamMode) + osmNoteQuests.filter(::isVisibleInTeamMode) + externalSourceQuests.filter(::isVisibleInTeamMode)
        } else {
            osmQuests + osmNoteQuests + externalSourceQuests
        }
    }

    fun get(questKey: QuestKey): Quest? = cache.get(questKey) ?: when (questKey) {
        is OsmNoteQuestKey -> osmNoteQuestSource.getVisible(questKey.noteId)
        is OsmQuestKey -> osmQuestSource.getVisible(questKey)
        is ExternalSourceQuestKey -> externalSourceQuestController.getVisible(questKey)
    }?.takeIf { isVisible(it, prefs.getBoolean(Prefs.HIDE_OVERLAY_QUESTS, true)) }

    private fun isVisible(questType: QuestType, hideOverlayQuests: Boolean): Boolean =
        visibleQuestTypeSource.isVisible(questType) &&
        selectedOverlaySource.selectedOverlay?.let { !hideOverlayQuests || questType.name !in it.hidesQuestTypes } ?: true

    private fun isVisible(quest: Quest, hideOverlayQuests: Boolean): Boolean =
        isVisibleInTeamMode(quest) && isVisible(quest.type, hideOverlayQuests)

    private fun isVisibleInTeamMode(quest: Quest): Boolean =
        teamModeQuestFilter.isVisible(quest) && levelFilter.isVisible(quest) && dayNightQuestFilter.isVisible(quest)

    fun getNearbyQuests(quest: Quest, distance: Double): Collection<Quest> {
        val bbox = quest.position.enclosingBoundingBox(distance)
        return when (prefs.getInt(Prefs.SHOW_NEARBY_QUESTS, 0)) {
            1 -> getAllVisible(bbox)
            2 -> (osmQuestSource.getAllVisibleInBBox(bbox) +
                    externalSourceQuestController.getAllInBBox(bbox) +
                    osmNoteQuestSource.getAllVisibleInBBox(bbox)
                ).filter { isVisibleInTeamMode(it) }
            3 -> (osmQuestSource.getAllVisibleInBBox(bbox, getHidden = true) +
                    externalSourceQuestController.getAllInBBox(bbox, getHidden = true) +
                    osmNoteQuestSource.getAllVisibleInBBox(bbox, getHidden = true)
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

    private fun updateVisibleQuests(addedQuests: Collection<Quest>, deletedQuestKeys: Collection<QuestKey>) {
        if (addedQuests.isEmpty() && deletedQuestKeys.isEmpty()) return
        if (addedQuests.size > 10 || deletedQuestKeys.size > 10) Log.i(TAG, "added ${addedQuests.size}, deleted ${deletedQuestKeys.size}")
            else Log.i(TAG, "added ${addedQuests.map { it.key }}, deleted: $deletedQuestKeys")
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

private const val TAG = "VisibleQuestsSource"
