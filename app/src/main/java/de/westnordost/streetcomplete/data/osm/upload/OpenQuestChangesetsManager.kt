package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.streetcomplete.ApplicationConstants.QUESTTYPE_TAG_KEY
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.changesets.OpenChangesetKey
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType

import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao.CLOSE_CHANGESETS_AFTER_INACTIVITY_OF
import javax.inject.Inject

/** Manages the creation and reusage of quest-related changesets */
class OpenQuestChangesetsManager @Inject constructor(
    private val osmDao: MapDataDao,
    private val openChangesetsDB: OpenChangesetsDao,
    private val changesetAutoCloser: ChangesetAutoCloser
) {
    private val TAG = "ChangesetManager"

    fun getOrCreateChangeset(questType: OsmElementQuestType<*>, source: String): Long {
        closeOldChangesets()
        val key = OpenChangesetKey(questType.name, source)
        val changesetInfo = openChangesetsDB.get(key)
        return if (changesetInfo?.changesetId != null) {
            changesetInfo.changesetId
        } else {
            createChangeset(questType, source)
        }
    }

    fun createChangeset(questType: OsmElementQuestType<*>, source: String): Long {
        val key = OpenChangesetKey(questType.name, source)
        val changesetId = osmDao.openChangeset(createChangesetTags(questType, source))
        openChangesetsDB.replace(key, changesetId)
        changesetAutoCloser.enqueue()
        Log.i(TAG, "Created changeset #$changesetId")
        return changesetId
    }

    @Synchronized fun closeOldChangesets() {
        val timePassed = System.currentTimeMillis() - openChangesetsDB.lastQuestSolvedTime
        if (timePassed < CLOSE_CHANGESETS_AFTER_INACTIVITY_OF) return

        for (info in openChangesetsDB.all) {
            try {
                osmDao.closeChangeset(info.changesetId)
                Log.i(TAG, "Closed changeset #${info.changesetId}")
            } catch (e: OsmConflictException) {
                Log.w(TAG, "Couldn't close changeset #${info.changesetId} because it has already been closed")
            } finally {
                openChangesetsDB.delete(info.key)
            }
        }
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
