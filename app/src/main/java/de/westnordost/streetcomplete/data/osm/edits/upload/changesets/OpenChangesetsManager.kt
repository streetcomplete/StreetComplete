package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.ApplicationConstants.QUESTTYPE_TAG_KEY
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import java.util.Locale

/** Manages the creation and reusage of changesets */
class OpenChangesetsManager(
    private val mapDataApi: MapDataApi,
    private val openChangesetsDB: OpenChangesetsDao,
    private val changesetAutoCloser: ChangesetAutoCloser,
    private val lastEditTimeStore: LastEditTimeStore
) {
    fun getOrCreateChangeset(type: ElementEditType, source: String): Long = synchronized(this) {
        val openChangeset = openChangesetsDB.get(type.name, source)
        return if (openChangeset?.changesetId != null) {
            openChangeset.changesetId
        } else {
            createChangeset(type, source)
        }
    }

    fun createChangeset(type: ElementEditType, source: String): Long = synchronized(this) {
        val changesetId = mapDataApi.openChangeset(createChangesetTags(type, source))
        openChangesetsDB.put(OpenChangeset(type.name, source, changesetId))
        changesetAutoCloser.enqueue(CLOSE_CHANGESETS_AFTER_INACTIVITY_OF)
        Log.i(TAG, "Created changeset #$changesetId")
        return changesetId
    }

    fun closeOldChangesets() = synchronized(this) {
        val timePassed = nowAsEpochMilliseconds() - lastEditTimeStore.get()
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

    private fun createChangesetTags(type: ElementEditType, source: String) =
        mapOf(
            "comment" to type.changesetComment,
            "created_by" to USER_AGENT,
            "locale" to Locale.getDefault().toLanguageTag(),
            QUESTTYPE_TAG_KEY to type.name,
            "source" to source
        )

    companion object {
        private const val TAG = "ChangesetManager"
    }
}

private const val CLOSE_CHANGESETS_AFTER_INACTIVITY_OF = 1000L * 60 * 20 // 20min
