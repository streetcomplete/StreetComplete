package de.westnordost.streetcomplete.data.osm.edits.upload

import android.util.Log
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.edits.*
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import javax.inject.Inject

class ElementEditsUploader @Inject constructor(
    private val elementEditsController: ElementEditsController,
    private val mapDataController: MapDataController,
    private val singleUploader: ElementEditUploader,
    private val mapDataApi: MapDataApi,
    private val statisticsUpdater: StatisticsUpdater
) {
    var uploadedChangeListener: OnUploadedChangeListener? = null

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("ElementEditsUploader"))

    suspend fun upload() = mutex.withLock { withContext(Dispatchers.IO) {
        while (true) {
            val edit = elementEditsController.getOldestUnsynced() ?: break
            val idProvider = elementEditsController.getIdProvider(edit.id)
            /* the sync of local change -> API and its response should not be cancellable because
             * otherwise an inconsistency in the data would occur. F.e. no "star" for an uploaded
             * change, a change could be uploaded twice etc */
            withContext(scope.coroutineContext) { uploadEdit(edit, idProvider) }
        }
    } }

    private suspend fun uploadEdit(edit: ElementEdit, idProvider: ElementIdProvider) {
        val questTypeName = edit.questType::class.simpleName!!
        val editActionClassName = edit.action::class.simpleName!!

        try {
            val updates = singleUploader.upload(edit, idProvider)

            Log.d(TAG, "Uploaded a $editActionClassName")
            uploadedChangeListener?.onUploaded(questTypeName, edit.position)

            elementEditsController.markSynced(edit, updates)
            mapDataController.updateAll(updates)

            if (edit.action is IsRevertAction) {
                statisticsUpdater.subtractOne(questTypeName, edit.position)
            } else {
                statisticsUpdater.addOne(questTypeName, edit.position)
            }

        } catch (e: ConflictException) {
            Log.d(TAG, "Dropped a $editActionClassName: ${e.message}")
            uploadedChangeListener?.onDiscarded(questTypeName, edit.position)

            elementEditsController.markSyncFailed(edit)

            val mapData = fetchElementComplete(edit.elementType, edit.elementId)
            if (mapData != null) {
                mapDataController.updateAll(MapDataUpdates(updated = mapData.toList()))
            } else {
                val elementKey = ElementKey(edit.elementType, edit.elementId)
                mapDataController.updateAll(MapDataUpdates(deleted = listOf(elementKey)))
            }
        }
    }

    private suspend fun fetchElementComplete(elementType: ElementType, elementId: Long): MapData? =
        withContext(Dispatchers.IO) {
            when (elementType) {
                ElementType.NODE -> mapDataApi.getNode(elementId)?.let { MutableMapData(listOf(it)) }
                ElementType.WAY -> mapDataApi.getWayComplete(elementId)
                ElementType.RELATION -> mapDataApi.getRelationComplete(elementId)
            }
        }

    companion object {
        private const val TAG = "ElementEditsUploader"
    }
}
