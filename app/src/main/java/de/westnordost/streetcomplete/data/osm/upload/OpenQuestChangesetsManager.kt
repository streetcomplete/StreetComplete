package de.westnordost.streetcomplete.data.osm.upload

import android.content.SharedPreferences
import android.util.Log

import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.map.MapDataDao
import de.westnordost.streetcomplete.ApplicationConstants.QUESTTYPE_TAG_KEY
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.changesets.OpenChangeset
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.toBcp47LanguageTag
import java.util.*

import javax.inject.Inject

/** Manages the creation and reusage of quest-related changesets */
class OpenQuestChangesetsManager @Inject constructor(
    private val osmDao: MapDataDao,
    private val openChangesetsDB: OpenChangesetsDao,
    private val changesetAutoCloser: ChangesetAutoCloser,
    private val prefs: SharedPreferences
) {
    fun getOrCreateChangeset(questType: OsmElementQuestType<*>, source: String): Long {
        val openChangeset = openChangesetsDB.get(questType.name, source)
        return if (openChangeset?.changesetId != null) {
            openChangeset.changesetId
        } else {
            createChangeset(questType, source)
        }
    }

    fun createChangeset(questType: OsmElementQuestType<*>, source: String): Long {
        val changesetId = osmDao.openChangeset(createChangesetTags(questType, source))
        openChangesetsDB.put(OpenChangeset(questType.name, source, changesetId))
        changesetAutoCloser.enqueue(CLOSE_CHANGESETS_AFTER_INACTIVITY_OF)
        Log.i(TAG, "Created changeset #$changesetId")
        return changesetId
    }

    @Synchronized fun closeOldChangesets() {
        val timePassed = System.currentTimeMillis() - prefs.getLong(Prefs.LAST_SOLVED_QUEST_TIME, 0)
        if (timePassed < CLOSE_CHANGESETS_AFTER_INACTIVITY_OF) return

        for (info in openChangesetsDB.getAll()) {
            try {
                osmDao.closeChangeset(info.changesetId)
                Log.i(TAG, "Closed changeset #${info.changesetId}")
            } catch (e: OsmConflictException) {
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
            "locale" to Locale.getDefault().toBcp47LanguageTag(),
            QUESTTYPE_TAG_KEY to questType.name,
            "source" to source
        )

    companion object {
        private const val TAG = "ChangesetManager"
    }
}

private const val CLOSE_CHANGESETS_AFTER_INACTIVITY_OF = 1000L * 60 * 20 // 20min

private val QuestType<*>.name get() = javaClass.simpleName
