package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log

import java.util.ArrayList
import java.util.HashSet
import java.util.concurrent.atomic.AtomicBoolean

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.ApplicationConstants.QUESTTYPE_TAG_KEY
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.changesets.OpenChangesetKey
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import de.westnordost.streetcomplete.util.SlippyMapMath

import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao.CLOSE_CHANGESETS_AFTER_INACTIVITY_OF
import javax.inject.Inject

class OsmQuestChangesetsUpload @Inject constructor(
    private val osmDao: MapDataDao,
    private val elementDB: MergedElementDao,
    private val elementGeometryDB: ElementGeometryDao,
    private val statisticsDB: QuestStatisticsDao,
    private val openChangesetsDB: OpenChangesetsDao,
    private val downloadedTilesDao: DownloadedTilesDao,
    private val changesetAutoCloser: ChangesetAutoCloser
) {
    private val TAG = "ChangesetUpload"

    var cancelState = AtomicBoolean()
    var visibleQuestListener: VisibleQuestListener? = null
    var uploadedChangeListener: OnUploadedChangeListener? = null

	@Synchronized fun upload(quests: Iterable<OsmQuest>, uploader: UploadFn) {
        var commits = 0
        var obsolete = 0
        val createdQuests: MutableList<OsmQuest> = ArrayList()
        val removedQuestIds: MutableList<Long> = ArrayList()

        val uploadedQuestTypes = HashSet<OsmElementQuestType<*>>()

        for (quest in quests) {
            if (cancelState.get()) break // break so that the unreferenced stuff is deleted still

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

        closeOpenChangesets()

        if (commits > 0) {
            changesetAutoCloser.enqueue()
        }
    }

	private fun uploadSingle(quest: OsmQuest, uploader: UploadFn): List<Element> {
		return try {
			uploader(getChangesetIdOrCreate(quest.osmElementQuestType, quest.changesSource), quest)
		} catch (e: OsmConflictException) {
			uploader(createChangeset(quest.osmElementQuestType, quest.changesSource), quest)
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

    @Synchronized fun closeOpenChangesets() {
        val timePassed = System.currentTimeMillis() - openChangesetsDB.lastQuestSolvedTime
        if (timePassed < CLOSE_CHANGESETS_AFTER_INACTIVITY_OF) return

        for (info in openChangesetsDB.all) {
            try {
                osmDao.closeChangeset(info.changesetId)
                Log.i(TAG, "Closed changeset #" + info.changesetId + ".")
            } catch (e: OsmConflictException) {
                Log.w(TAG, "Couldn't close changeset #" + info.changesetId + " because it has already been closed.")
            } finally {
                openChangesetsDB.delete(info.key)
            }
        }
    }

    private fun getChangesetIdOrCreate(questType: OsmElementQuestType<*>, source: String): Long {
        val key = OpenChangesetKey(questType.name, source)
	    val changesetInfo = openChangesetsDB.get(key)
	    return if (changesetInfo?.changesetId != null) {
		    changesetInfo.changesetId
	    } else {
		    createChangeset(questType, source)
	    }
    }

    private fun createChangeset(questType: OsmElementQuestType<*>, source: String): Long {
        val key = OpenChangesetKey(questType.name, source)
        val changesetId = osmDao.openChangeset(createChangesetTags(questType, source))
        openChangesetsDB.replace(key, changesetId)
        return changesetId
    }

    private fun invalidateAreaAroundQuest(quest: OsmQuest) {
        // called after a conflict. If there is a conflict, the user is not the only one in that
        // area, so best invalidate all downloaded quests here and redownload on next occasion
        val questPosition = quest.geometry.center
        val tile = SlippyMapMath.enclosingTile(questPosition, ApplicationConstants.QUEST_TILE_ZOOM)
        downloadedTilesDao.remove(tile)
    }

    private fun createChangesetTags(questType: OsmElementQuestType<*>, source: String) = 
        mapOf(
            "comment" to questType.commitMessage,
            "created_by" to USER_AGENT,
            QUESTTYPE_TAG_KEY to questType.name,
            "source" to source
        )
}

private val QuestType<*>.name get() = javaClass.simpleName

private typealias UploadFn = (changesetId: Long, quest: OsmQuest) -> List<Element>
