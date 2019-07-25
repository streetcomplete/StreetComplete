package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log

import java.util.ArrayList
import java.util.HashSet
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.util.SlippyMapMath

import javax.inject.Inject

class OsmQuestChangesetsUpload @Inject constructor(
    private val elementDB: MergedElementDao,
    private val elementGeometryDB: ElementGeometryDao,
    private val statisticsDB: QuestStatisticsDao,
    private val downloadedTilesDao: DownloadedTilesDao
) {
    private val TAG = "ChangesetUpload"
/*
    var visibleQuestListener: VisibleQuestListener? = null
    var uploadedChangeListener: OnUploadedChangeListener? = null

	@Synchronized fun upload(quests: Iterable<OsmQuest>, uploader: UploadFn) {
        var commits = 0
        var obsolete = 0
        val createdQuests: MutableList<OsmQuest> = ArrayList()
        val removedQuestIds: MutableList<Long> = ArrayList()

        val uploadedQuestTypes = HashSet<OsmElementQuestType<*>>()

        for (quest in quests) {
            // was deleted while trying to upload another quest
            if (removedQuestIds.contains(quest.id)) continue

            val elements = uploadSingle(quest, uploader)
            createdQuests.addAll(uploadResult.getCreatedQuests())
            removedQuestIds.addAll(uploadResult.getRemovedQuestIds())
            if (uploadResult.success) {
                uploadedQuestTypes.add(quest.osmElementQuestType)
                uploadedChangeListener?.onUploaded()
                statisticsDB.addOne(quest.type.name)
                commits++
            } else {
                uploadedChangeListener?.onDiscarded()
                obsolete++
                invalidateAreaAroundQuest(quest)
            }
        }

        cleanUp(uploadedQuestTypes)

        var logMsg = "Committed $commits changes"
        if (obsolete > 0) {
            logMsg += " but dropped $obsolete changes because there were conflicts"
        }

        Log.i(TAG, logMsg)

        if (!createdQuests.isEmpty()) {
	        visibleQuestListener?.onQuestsCreated(createdQuests, QuestGroup.OSM)
            Log.i(TAG, "Created ${createdQuests.size} new quests")
        }
        if (!removedQuestIds.isEmpty()) {
            visibleQuestListener?.onQuestsRemoved(removedQuestIds, QuestGroup.OSM)
            Log.i(TAG, "Removed ${removedQuestIds.size} quests which are no longer applicable")
        }
    }

    private fun cleanUp(questTypes: Set<OsmElementQuestType<*>>) {
        elementGeometryDB.deleteUnreferenced()
        elementDB.deleteUnreferenced()
        // must be after unreferenced elements have been deleted
        for (questType in questTypes) {
            questType.cleanMetadata()
        }
    }

    private fun invalidateAreaAroundQuest(quest: OsmQuest) {
        // called after a conflict. If there is a conflict, the user is not the only one in that
        // area, so best invalidate all downloaded quests here and redownload on next occasion
        val questPosition = quest.center
        val tile = SlippyMapMath.enclosingTile(questPosition, ApplicationConstants.QUEST_TILE_ZOOM)
        downloadedTilesDao.remove(tile)
    }

    */
}

private val QuestType<*>.name get() = javaClass.simpleName

