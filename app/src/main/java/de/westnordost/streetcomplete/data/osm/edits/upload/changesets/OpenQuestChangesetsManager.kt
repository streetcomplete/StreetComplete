package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants.QUESTTYPE_TAG_KEY
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.upload.ConflictException
import java.util.Locale
import javax.inject.Inject

/** Manages the creation and reusage of quest-related changesets */
class OpenQuestChangesetsManager @Inject constructor(
    private val mapDataApi: MapDataApi,
    private val openChangesetsDB: OpenChangesetsDao,
    private val changesetAutoCloser: ChangesetAutoCloser,
    private val lastEditTimeStore: LastEditTimeStore
) {
    fun getOrCreateChangeset(questType: OsmElementQuestType<*>, source: String): Long  = synchronized(this) {
        val openChangeset = openChangesetsDB.get(questType.name, source)
        return if (openChangeset?.changesetId != null) {
            openChangeset.changesetId
        } else {
            createChangeset(questType, source)
        }
    }

    fun createChangeset(questType: OsmElementQuestType<*>, source: String): Long = synchronized(this) {
        val changesetId = mapDataApi.openChangeset(createChangesetTags(questType, source))
        openChangesetsDB.put(OpenChangeset(questType.name, source, changesetId))
        changesetAutoCloser.enqueue(CLOSE_CHANGESETS_AFTER_INACTIVITY_OF)
        Log.i(TAG, "Created changeset #$changesetId")
        return changesetId
    }

    fun closeOldChangesets() = synchronized(this) {
        val timePassed = System.currentTimeMillis() - lastEditTimeStore.get()
        if (timePassed < CLOSE_CHANGESETS_AFTER_INACTIVITY_OF) return

        for (info in openChangesetsDB.getAll()) {
            try {
                mapDataApi.closeChangeset(info.changesetId)
                Log.i(TAG, "Closed changeset #${info.changesetId}")
            } catch (e: ConflictException) {
                Log.w(TAG, "Couldn't close changeset #${info.changesetId} because it has already been closed")
            } finally {
                openChangesetsDB.delete(info.questType, info.source)
            }
        }
    }

    private fun createChangesetTags(questType: OsmElementQuestType<*>, source: String) =
        mapOf(
            "comment" to questType.commitMessage,
            "created_by" to USER_AGENT,
            "locale" to Locale.getDefault().toLanguageTag(),
            QUESTTYPE_TAG_KEY to questType.name,
            "source" to source
        )

    companion object {
        private const val TAG = "ChangesetManager"
    }
}

private const val CLOSE_CHANGESETS_AFTER_INACTIVITY_OF = 1000L * 60 * 20 // 20min

private val QuestType<*>.name get() = javaClass.simpleName
