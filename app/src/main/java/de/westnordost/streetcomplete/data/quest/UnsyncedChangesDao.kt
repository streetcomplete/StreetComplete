package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestDao
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayDao
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestDao
import javax.inject.Inject

class UnsyncedChangesDao @Inject constructor(
    private val questDao: OsmQuestDao,
    private val noteQuestDao: OsmNoteQuestDao,
    private val createNoteDao: CreateNoteDao,
    private val splitWayDao: OsmQuestSplitWayDao
) {
    fun getCount(): Int =
        questDao.getCount(listOf(QuestStatus.ANSWERED)) +
        noteQuestDao.getCount(listOf(QuestStatus.ANSWERED)) +
        splitWayDao.getCount() +
        createNoteDao.getCount()
}