package de.westnordost.streetcomplete.data.osm.splitway

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.elementgeometry.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.OsmInChangesetsUploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/** Gets all split ways from local DB and uploads them via the OSM API */
class SplitWaysUploader @Inject constructor(
        private val elementDB: MergedElementDao,
        private val elementGeometryDB: ElementGeometryDao,
        changesetManager: OpenQuestChangesetsManager,
        private val questGiver: OsmQuestGiver,
        private val osmApiElementGeometryCreator: OsmApiElementGeometryCreator,
        private val splitWayDB: OsmQuestSplitWayDao,
        private val splitSingleOsmWayUploader: SplitSingleWayUploader,
        private val statisticsUpdater: StatisticsUpdater
) : OsmInChangesetsUploader<OsmQuestSplitWay>(elementDB, elementGeometryDB, changesetManager,
    questGiver, osmApiElementGeometryCreator) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Splitting ways")
        super.upload(cancelled)
    }

    override fun getAll(): Collection<OsmQuestSplitWay> = splitWayDB.getAll()

    override fun uploadSingle(changesetId: Long, quest: OsmQuestSplitWay, element: Element): List<Element> {
        return splitSingleOsmWayUploader.upload(changesetId, element as Way, quest.splits)
    }

    override fun updateElement(newElement: Element, quest: OsmQuestSplitWay) {
        val geometry = osmApiElementGeometryCreator.create(newElement)
        if (geometry != null) {
            elementGeometryDB.put(ElementGeometryEntry(newElement.type, newElement.id, geometry))
            elementDB.put(newElement)
            questGiver.recreateQuests(newElement, geometry, quest.questTypesOnWay)
        } else {
            // new element has invalid geometry
            elementDB.delete(newElement.type, newElement.id)
            questGiver.deleteQuests(newElement.type, newElement.id)
        }
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
