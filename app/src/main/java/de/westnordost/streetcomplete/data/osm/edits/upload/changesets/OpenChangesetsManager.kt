package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.ApplicationConstants.QUESTTYPE_TAG_KEY
import de.westnordost.streetcomplete.ApplicationConstants.USER_AGENT
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.upload.LastEditTimeStore
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataApi
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.distanceTo
import java.util.Locale

/** Manages the creation and reusage of changesets */
class OpenChangesetsManager(
    private val mapDataApi: MapDataApi,
    private val openChangesetsDB: OpenChangesetsDao,
    private val changesetAutoCloser: ChangesetAutoCloser,
    private val lastEditTimeStore: LastEditTimeStore
) {
    fun getOrCreateChangeset(
        type: ElementEditType,
        source: String,
        position: LatLon,
        createNewIfTooFarAway: Boolean
    ): Long = synchronized(this) {
        val openChangeset = openChangesetsDB.get(type.name, source)
            ?: return createChangeset(type, source, position)

        if (createNewIfTooFarAway && position.distanceTo(openChangeset.lastPosition) > MAX_LAST_EDIT_DISTANCE) {
            closeChangeset(openChangeset)
            return createChangeset(type, source, position)
        } else {
            return openChangeset.changesetId
        }
    }

    fun createChangeset(
        type: ElementEditType,
        source: String,
        position: LatLon
    ): Long = synchronized(this) {
        val changesetId = mapDataApi.openChangeset(createChangesetTags(type, source))
        openChangesetsDB.put(OpenChangeset(type.name, source, changesetId, position))
        changesetAutoCloser.enqueue(CLOSE_CHANGESETS_AFTER_INACTIVITY_OF)
        Log.i(TAG, "Created changeset #$changesetId")
        return changesetId
    }

    fun closeOldChangesets() = synchronized(this) {
        val timePassed = nowAsEpochMilliseconds() - lastEditTimeStore.get()
        if (timePassed < CLOSE_CHANGESETS_AFTER_INACTIVITY_OF) return

        for (info in openChangesetsDB.getAll()) {
            closeChangeset(info)
        }
    }

    private fun closeChangeset(openChangeset: OpenChangeset) {
        try {
            mapDataApi.closeChangeset(openChangeset.changesetId)
            Log.i(TAG, "Closed changeset #${openChangeset.changesetId}")
        } catch (e: ConflictException) {
            Log.w(TAG, "Couldn't close changeset #${openChangeset.changesetId} because it has already been closed")
        } finally {
            openChangesetsDB.delete(openChangeset.questType, openChangeset.source)
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

private const val MAX_LAST_EDIT_DISTANCE = 5000 // 5km
