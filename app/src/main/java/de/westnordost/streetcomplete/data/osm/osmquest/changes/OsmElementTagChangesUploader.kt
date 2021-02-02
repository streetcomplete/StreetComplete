package de.westnordost.streetcomplete.data.osm.osmquest.changes

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.OsmInChangesetsUploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all undo osm quests from local DB and uploads them via the OSM API */
class ElementTagChangesUploader @Inject constructor(
    changesetManager: OpenQuestChangesetsManager,
    osmElementController: OsmElementController,
    private val osmElementTagChangesDB: OsmElementTagChangesDao,
    private val singleChangeUploader: SingleOsmElementTagChangesUploader,
    private val statisticsUpdater: StatisticsUpdater
) : OsmInChangesetsUploader<OsmElementTagChanges>(changesetManager, osmElementController) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Applying element changes")
        super.upload(cancelled)
    }

    override fun getAll() = osmElementTagChangesDB.getAll()

    override fun uploadSingle(changesetId: Long, quest: OsmElementTagChanges, element: Element): List<Element> {
        return listOf(singleChangeUploader.upload(changesetId, quest, element))
    }

    override fun onUploadSuccessful(quest: OsmElementTagChanges) {
        osmElementTagChangesDB.delete(quest.id!!)
        if (quest.isRevert) {
            statisticsUpdater.subtractOne(quest.osmElementQuestType.javaClass.simpleName, quest.position)
        } else {
            statisticsUpdater.addOne(quest.osmElementQuestType.javaClass.simpleName, quest.position)
        }
        Log.d(TAG, "Uploaded element tag change ${quest.toLogString()}")
    }

    override fun onUploadFailed(quest: OsmElementTagChanges, e: Throwable) {
        osmElementTagChangesDB.delete(quest.id!!)
        Log.d(TAG, "Dropped element tag change ${quest.toLogString()}: ${e.message}")
    }

    companion object {
        private const val TAG = "ElementTagChangesUpload"
    }
}

private fun OsmElementTagChanges.toLogString() =
    osmElementQuestType.javaClass.simpleName + " for " + elementType.name.toLowerCase(Locale.US) + " #" + elementId

