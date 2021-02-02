package de.westnordost.streetcomplete.data.osm.splitway

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.OsmInChangesetsUploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/** Gets all split ways from local DB and uploads them via the OSM API */
class SplitOsmWaysUploader @Inject constructor(
    changesetManager: OpenQuestChangesetsManager,
    private val osmElementController: OsmElementController,
    private val splitWayDB: SplitOsmWayDao,
    private val splitSingleOsmOsmWayUploader: SplitSingleOsmWayUploader,
    private val statisticsUpdater: StatisticsUpdater
) : OsmInChangesetsUploader<SplitOsmWay>(changesetManager, osmElementController) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Splitting ways")
        super.upload(cancelled)
    }

    override fun getAll(): Collection<SplitOsmWay> = splitWayDB.getAll()

    override fun uploadSingle(changesetId: Long, split: SplitOsmWay, element: Element): List<Element> {
        return splitSingleOsmOsmWayUploader.upload(changesetId, element as Way, split.splits)
    }

    override fun onUploadSuccessful(quest: SplitOsmWay) {
        splitWayDB.delete(quest.id!!)
        statisticsUpdater.addOne(quest.osmElementQuestType.javaClass.simpleName, quest.position)
        Log.d(TAG, "Uploaded split way #${quest.wayId}")
    }

    override fun onUploadFailed(quest: SplitOsmWay, e: Throwable) {
        splitWayDB.delete(quest.id!!)
        Log.d(TAG, "Dropped split for way #${quest.wayId}: ${e.message}")
    }

    companion object {
        private const val TAG = "SplitOsmWayUpload"
    }
}
