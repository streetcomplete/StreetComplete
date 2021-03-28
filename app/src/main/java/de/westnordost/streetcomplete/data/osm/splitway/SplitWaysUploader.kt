package de.westnordost.streetcomplete.data.osm.splitway

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementUpdateController
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.OsmInChangesetsUploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/** Gets all split ways from local DB and uploads them via the OSM API */
class SplitWaysUploader @Inject constructor(
    changesetManager: OpenQuestChangesetsManager,
    private val elementUpdateController: OsmElementUpdateController,
    private val splitWayDB: OsmQuestSplitWayDao,
    private val splitSingleOsmWayUploader: SplitSingleWayUploader,
    private val statisticsUpdater: StatisticsUpdater
) : OsmInChangesetsUploader<OsmQuestSplitWay>(changesetManager, elementUpdateController) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Splitting ways")
        super.upload(cancelled)
    }

    override fun getAll(): Collection<OsmQuestSplitWay> = splitWayDB.getAll()

    override fun uploadSingle(changesetId: Long, quest: OsmQuestSplitWay, element: Element): List<Element> {
        return splitSingleOsmWayUploader.upload(changesetId, element as Way, quest.splits)
    }

    override fun updateElement(element: Element, quest: OsmQuestSplitWay) {
        /* We override this because in case of a split, the two (or more) sections of the way should
        *  actually get the same quests as the original way, there is no need to again check for
        *  the eligibility of the element for each quest which would be done normally */
        elementUpdateController.update(element, quest.questTypesOnWay)
    }

    override fun onUploadSuccessful(quest: OsmQuestSplitWay) {
        splitWayDB.delete(quest.questId)
        statisticsUpdater.addOne(quest.osmElementQuestType.javaClass.simpleName, quest.position)
        Log.d(TAG, "Uploaded split way #${quest.wayId}")
    }

    override fun onUploadFailed(quest: OsmQuestSplitWay, e: Throwable) {
        splitWayDB.delete(quest.questId)
        Log.d(TAG, "Dropped split for way #${quest.wayId}: ${e.message}")
    }

    companion object {
        private const val TAG = "SplitOsmWayUpload"
    }
}
