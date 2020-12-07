package de.westnordost.streetcomplete.data.osm.delete_element

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmElement
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementUpdateController
import de.westnordost.streetcomplete.data.osm.upload.OsmInChangesetsUploader
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class DeleteOsmElementsUploader @Inject constructor(
    changesetManager: OpenQuestChangesetsManager,
    private val elementUpdateController: OsmElementUpdateController,
    private val deleteElementDB: DeleteOsmElementDao,
    private val singleUploader: DeleteSingleOsmElementUploader,
    private val statisticsUpdater: StatisticsUpdater
) : OsmInChangesetsUploader<DeleteOsmElement>(changesetManager, elementUpdateController) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Deleting Elements")
        super.upload(cancelled)
    }

    override fun getAll(): Collection<DeleteOsmElement> = deleteElementDB.getAll()

    override fun uploadSingle(changesetId: Long, quest: DeleteOsmElement, element: Element): List<Element> {
        singleUploader.upload(changesetId, element as OsmElement)
        return listOf(element)
    }

    override fun updateElement(element: Element, quest: DeleteOsmElement) {
        /* So on successful upload of deleting the element, all that refers to that element should
        *  be removed as well */
        elementUpdateController.delete(element.type, element.id)
    }

    override fun onUploadSuccessful(quest: DeleteOsmElement) {
        deleteElementDB.delete(quest.questId)
        statisticsUpdater.addOne(quest.osmElementQuestType.javaClass.simpleName, quest.position)
        Log.d(TAG, "Uploaded delete element for ${quest.elementType.name} #${quest.elementId}")
    }

    override fun onUploadFailed(quest: DeleteOsmElement, e: Throwable) {
        deleteElementDB.delete(quest.questId)
        Log.d(TAG, "Dropped delete element for ${quest.elementType.name} #${quest.elementId}: ${e.message}")
    }

    companion object {
        private const val TAG = "DeleteOsmElementUpload"
    }
}
