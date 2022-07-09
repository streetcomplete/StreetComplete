package de.westnordost.streetcomplete.data.quest

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.visiblequests.DayNightQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import java.util.concurrent.CopyOnWriteArrayList

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

    init {
        osmQuestSource.addListener(osmQuestSourceListener)
        osmNoteQuestSource.addListener(osmNoteQuestSourceListener)
        visibleQuestTypeSource.addListener(visibleQuestTypeSourceListener)
        teamModeQuestFilter.addListener(teamModeQuestFilterListener)
        selectedOverlaySource.addListener(selectedOverlayListener)
    }

    /** Retrieve all visible quests in the given bounding box from local database */
    fun getAllVisible(bbox: BoundingBox): List<Quest> {
        val visibleQuestTypeNames = questTypeRegistry
            .filter { isVisible(it) }
            .map { it.name }
        if (visibleQuestTypeNames.isEmpty()) return listOf()

        val osmQuests = osmQuestSource.getAllVisibleInBBox(bbox, visibleQuestTypeNames)
        val osmNoteQuests = osmNoteQuestSource.getAllVisibleInBBox(bbox)

        return if (teamModeQuestFilter.isEnabled || levelFilter.isEnabled || dayNightQuestFilter.isEnabled) {
            osmQuests.filter(::isVisibleInTeamMode) + osmNoteQuests.filter(::isVisibleInTeamMode)
        } else {
            osmQuests + osmNoteQuests
        }
    }

    fun get(questKey: QuestKey): Quest? = when (questKey) {
        is OsmNoteQuestKey -> osmNoteQuestSource.get(questKey.noteId)
        is OsmQuestKey -> osmQuestSource.get(questKey)
    }?.takeIf { isVisible(it) }

    private fun isVisible(questType: QuestType): Boolean =
        visibleQuestTypeSource.isVisible(questType) &&
        selectedOverlaySource.selectedOverlay?.let { questType.name !in it.hidesQuestTypes } ?: true


    private fun isVisible(quest: Quest): Boolean =
        isVisibleInTeamMode(quest) && isVisible(quest.type)

    private fun isVisibleInTeamMode(quest: Quest): Boolean =
        teamModeQuestFilter.isVisible(quest) && levelFilter.isVisible(quest) && dayNightQuestFilter.isVisible(quest)

    fun getNearbyQuests(quest: OsmQuest, distance: Double) = when (prefs.getInt(Prefs.SHOW_NEARBY_QUESTS, 0)) {
        1 -> getAllVisible(quest.position.enclosingBoundingBox(distance)).filterIsInstance<OsmQuest>()
        2 -> osmQuestSource.getAllVisibleInBBox(quest.position.enclosingBoundingBox(distance), null)
        3 -> osmQuestSource.getAllNearbyQuests(quest, distance)
        else -> emptyList()
    }


    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun updateVisibleQuests(addedQuests: Collection<Quest>, deletedQuestKeys: Collection<QuestKey>) {
        if (addedQuests.isEmpty() && deletedQuestKeys.isEmpty()) return
        listeners.forEach { it.onUpdatedVisibleQuests(addedQuests, deletedQuestKeys) }
    }

    private fun invalidate() {
        listeners.forEach { it.onVisibleQuestsInvalidated() }
    }
}
