package de.westnordost.streetcomplete.data.osm.upload

import android.os.CancellationSignal
import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.UndoOsmQuest
import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import java.util.*

/** Gets all undo osm quests from local DB and uploads them via the OSM API */
class UndoOsmQuestsUpload @Inject constructor(
    private val undoQuestDB: UndoOsmQuestDao,
    private val elementDB: MergedElementDao,
    private val changesetManager: OpenQuestChangesetsManager,
    private val singleChangeUpload: SingleOsmElementTagChangesUpload
) {
    private val TAG = "UndoOsmQuestUpload"

    var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized fun upload(signal: CancellationSignal) {
        if (signal.isCanceled) return
        Log.i(TAG, "Undoing quest changes")
        for (quest in undoQuestDB.getAll()) {
            if (signal.isCanceled) break

            try {
                uploadSingle(quest)
                Log.d(TAG, "Uploaded undo osm quest ${quest.toLogString()}")
                uploadedChangeListener?.onUploaded()
            } catch (e: ElementConflictException) {
                Log.d(TAG, "Dropped undo osm quest ${quest.toLogString()}: ${e.message}")
                uploadedChangeListener?.onDiscarded()
            }

            undoQuestDB.delete(quest.id!!)
        }
    }

    private fun uploadSingle(quest: UndoOsmQuest): Element {
        val element = elementDB.get(quest.elementType, quest.elementId)
            ?: throw ElementDeletedException("Element deleted")

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(quest.type, quest.changesSource)
            singleChangeUpload.upload(changesetId, quest, element)
        }  catch (e: ChangesetConflictException) {
            val changesetId = changesetManager.createChangeset(quest.type, quest.changesSource)
            singleChangeUpload.upload(changesetId, quest, element)
        }
    }
}

private fun UndoOsmQuest.toLogString() =
    type.javaClass.simpleName + " for " + elementType.name.toLowerCase(Locale.US) + " #" + elementId

