package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.OsmQuest
import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao
import java.util.*

class UndoOsmQuestsUpload @Inject constructor(
    private val undoQuestDB: UndoOsmQuestDao,
    private val elementDB: MergedElementDao,
    private val singleOsmQuestChangeUpload: SingleOsmQuestUpload
) {
    private val TAG = "UndoOsmQuestUpload"

    fun upload(changesetsUpload: OsmQuestChangesetsUpload) {
        Log.i(TAG, "Undoing quest changes")

        val quests = undoQuestDB.getAll()
        changesetsUpload.upload(quests, this::uploadSingle)
    }

    private fun uploadSingle(changesetId: Long, quest: OsmQuest) : List<Element> {
        val element = elementDB.get(quest.elementType, quest.elementId)

        try {
            if (element == null) throw ElementDeletedException("Element deleted")
            val updatedElement = singleOsmQuestChangeUpload.upload(changesetId, quest, element)
            return listOf(updatedElement)
        }
        catch (e: ElementConflictException) {
            Log.d(TAG, "Dropping osm quest ${quest.toLogString()}: ${e.message}")
            throw e
        }
        finally {
            undoQuestDB.delete(quest.id)
        }
    }
}

private fun OsmQuest.toLogString() =
    type.javaClass.simpleName + " for " + elementType.name.toLowerCase(Locale.US) + " #" + elementId

