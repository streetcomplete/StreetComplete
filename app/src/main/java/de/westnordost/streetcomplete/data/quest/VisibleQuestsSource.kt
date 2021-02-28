package de.westnordost.streetcomplete.data.quest

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestSource
import de.westnordost.streetcomplete.data.visiblequests.TeamModeQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Access and listen to quests visible on the map */
@Singleton class VisibleQuestsSource @Inject constructor(
    private val osmQuestSource: OsmQuestSource,
    private val osmNoteQuestSource: OsmNoteQuestSource,
    private val visibleQuestTypeSource: VisibleQuestTypeSource,
    private val teamModeQuestFilter: TeamModeQuestFilter
) {
    interface Listener {
        /** Called when given quests in the given group have been added/removed */
        fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<Long>, group: QuestGroup)
        /** Called when something has changed which should trigger any listeners to update all */
        fun onVisibleQuestsInvalidated()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    private val osmQuestSourceListener = object : OsmQuestSource.Listener {
        override fun onUpdated(addedQuests: Collection<OsmQuest>, deletedQuestIds: Collection<Long>) {
            updateVisibleQuests(addedQuests.filter(::isVisible), deletedQuestIds, QuestGroup.OSM)
        }
    }

    private val osmNoteQuestSourceListener = object : OsmNoteQuestSource.Listener {
        override fun onUpdated(addedQuests: Collection<OsmNoteQuest>, deletedQuestIds: Collection<Long>) {
            updateVisibleQuests(addedQuests.filter(::isVisible), deletedQuestIds, QuestGroup.OSM_NOTE)
        }
        override fun onInvalidated() {
            // apparently the visibility of many different notes have changed
            invalidate()
        }
    }

    private val visibleQuestTypeSourceListener = object : VisibleQuestTypeSource.Listener {
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

    init {
        osmQuestSource.addListener(osmQuestSourceListener)
        osmNoteQuestSource.addListener(osmNoteQuestSourceListener)
        visibleQuestTypeSource.addListener(visibleQuestTypeSourceListener)
        teamModeQuestFilter.addListener(teamModeQuestFilterListener)
    }

    /** Get count of all visible quests in given bounding box */
    fun getCount(bbox: BoundingBox): Int =
        osmQuestSource.getAllInBBoxCount(bbox)

    /** Retrieve all visible quests in the given bounding box from local database */
    fun getAllVisible(bbox: BoundingBox, questTypes: Collection<String>): List<QuestAndGroup> {
        if (questTypes.isEmpty()) return listOf()
        val osmQuests = osmQuestSource.getAllVisibleInBBox(bbox, questTypes)
        val osmNoteQuests = osmNoteQuestSource.getAllVisibleInBBox(bbox)

        return osmQuests.filter(::isVisible).map { QuestAndGroup(it, QuestGroup.OSM) } +
               osmNoteQuests.filter(::isVisible).map { QuestAndGroup(it, QuestGroup.OSM_NOTE) }
    }

    fun get(questGroup: QuestGroup, questId: Long): Quest? = when (questGroup) {
        QuestGroup.OSM -> osmQuestSource.get(questId)
        QuestGroup.OSM_NOTE -> osmNoteQuestSource.get(questId)
    }?.takeIf(::isVisible)

    private fun isVisible(quest: Quest): Boolean =
        visibleQuestTypeSource.isVisible(quest.type) && teamModeQuestFilter.isVisible(quest)

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun updateVisibleQuests(addedQuests: Collection<Quest>, deletedQuestIds: Collection<Long>, group: QuestGroup) {
        if (addedQuests.isEmpty() && deletedQuestIds.isEmpty()) return
        listeners.forEach { it.onUpdatedVisibleQuests(addedQuests, deletedQuestIds, group) }
    }

    private fun invalidate() {
        listeners.forEach { it.onVisibleQuestsInvalidated() }
    }
}


