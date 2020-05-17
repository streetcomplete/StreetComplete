package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.elementgeometry.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.OsmInChangesetsUploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all answered osm quests from local DB and uploads them via the OSM API */
class OsmQuestsUploader @Inject constructor(
        elementDB: MergedElementDao,
        elementGeometryDB: ElementGeometryDao,
        changesetManager: OpenQuestChangesetsManager,
        questGiver: OsmQuestGiver,
        osmApiElementGeometryCreator: OsmApiElementGeometryCreator,
        private val osmQuestController: OsmQuestController,
        private val singleChangeUploader: SingleOsmElementTagChangesUploader,
        private val statisticsUpdater: StatisticsUpdater
) : OsmInChangesetsUploader<OsmQuest>(elementDB, elementGeometryDB, changesetManager, questGiver,
    osmApiElementGeometryCreator) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Applying quest changes")
        super.upload(cancelled)
    }

    override fun getAll(): Collection<OsmQuest> = osmQuestController.getAllAnswered()

    override fun uploadSingle(changesetId: Long, quest: OsmQuest, element: Element): List<Element> {
        return listOf(singleChangeUploader.upload(changesetId, quest, element))
    }

    override fun onUploadSuccessful(quest: OsmQuest) {
        osmQuestController.success(quest)
        statisticsUpdater.addOne(quest.osmElementQuestType.javaClass.simpleName, quest.center)
        Log.d(TAG, "Uploaded osm quest ${quest.toLogString()}")
    }

    override fun onUploadFailed(quest: OsmQuest, e: Throwable) {
        osmQuestController.fail(quest)
        Log.d(TAG, "Dropped osm quest ${quest.toLogString()}: ${e.message}")
    }

    override fun cleanUp(questTypes: Set<OsmElementQuestType<*>>) {
        super.cleanUp(questTypes)
        osmQuestController.cleanUp()
    }

    companion object {
        private const val TAG = "OsmQuestUpload"
    }
}

private fun OsmQuest.toLogString() =
    type.javaClass.simpleName + " for " + elementType.name.toLowerCase(Locale.US) + " #" + elementId
