package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.OsmQuestSplitWay
import de.westnordost.streetcomplete.data.osm.download.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/** Gets all split ways from local DB and uploads them via the OSM API */
class SplitWaysUploader @Inject constructor(
    elementDB: MergedElementDao,
    elementGeometryDB: ElementGeometryDao,
    changesetManager: OpenQuestChangesetsManager,
    questGiver: OsmQuestGiver,
    statisticsDB: QuestStatisticsDao,
    osmApiElementGeometryCreator: OsmApiElementGeometryCreator,
    private val splitWayDB: OsmQuestSplitWayDao,
    private val splitSingleOsmWayUpload: SplitSingleWayUpload
) : OsmInChangesetsUploader<OsmQuestSplitWay>(elementDB, elementGeometryDB, changesetManager,
    questGiver, statisticsDB, osmApiElementGeometryCreator) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Splitting ways")
        super.upload(cancelled)
    }

    override fun getAll(): Collection<OsmQuestSplitWay> = splitWayDB.getAll()

    override fun uploadSingle(changesetId: Long, quest: OsmQuestSplitWay, element: Element): List<Element> {
        return splitSingleOsmWayUpload.upload(changesetId, element as Way, quest.splits)
    }

    override fun onUploadSuccessful(quest: OsmQuestSplitWay) {
        splitWayDB.delete(quest.questId)
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
