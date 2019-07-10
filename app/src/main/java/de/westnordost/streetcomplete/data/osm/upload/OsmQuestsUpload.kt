package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ApplicationConstants.MAX_QUEST_UNDO_HISTORY_AGE
import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.persist.AOsmQuestDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import java.util.*

open class OsmQuestsUpload @Inject constructor(
    private val questDB: AOsmQuestDao,
    private val elementDB: MergedElementDao,
    private val singleOsmQuestChangeUpload: SingleOsmQuestUpload
) {
    private val TAG = "OsmQuestUpload"

	fun upload(changesetsUpload: OsmQuestChangesetsUpload) {
		Log.i(TAG, "Applying quest changes")

        val quests = questDB.getAll(null, QuestStatus.ANSWERED)
		changesetsUpload.upload(quests, this::uploadSingle)

		questDB.deleteAllClosed(System.currentTimeMillis() - MAX_QUEST_UNDO_HISTORY_AGE)
	}

    private fun uploadSingle(changesetId: Long, quest: OsmQuest) : List<Element> {
        val element = elementDB.get(quest.elementType, quest.elementId)

        try {
            if (element == null) throw ElementDeletedException("Element deleted")

            val updatedElement = singleOsmQuestChangeUpload.upload(changesetId, quest, element)

            quest.status = QuestStatus.CLOSED
            questDB.update(quest)
            return listOf(updatedElement)
        }
        catch (e: ElementConflictException) {
            Log.d(TAG, "Dropping osm quest ${quest.toLogString()}: ${e.message}")
            /* #812 conflicting quests may not reside in the database, otherwise they would
               wrongfully be candidates for an undo - even though nothing was changed */
            questDB.delete(quest.id!!)

            throw e
        }
    }
}

private fun OsmQuest.toLogString() =
    type.javaClass.simpleName + " for " + elementType.name.toLowerCase(Locale.US) + " #" + elementId
