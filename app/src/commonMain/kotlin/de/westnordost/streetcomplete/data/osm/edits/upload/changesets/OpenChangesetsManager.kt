package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.distanceTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/** Manages the creation and reusage of changesets */
class OpenChangesetsManager(
    private val changesetApiClient: ChangesetApiClient,
    private val openChangesetsDB: OpenChangesetsDao,
    private val changesetAutoCloser: ChangesetAutoCloser,
    private val prefs: Preferences
) {
    suspend fun getOrCreateChangeset(
        type: ElementEditType,
        source: String,
        position: LatLon,
        createNewIfTooFarAway: Boolean
    ): Long {
        val openChangeset = withContext(Dispatchers.IO) { openChangesetsDB.get(type.name, source) }
            ?: return createChangeset(type, source, position)

        if (createNewIfTooFarAway && position.distanceTo(openChangeset.lastPosition) > ApplicationConstants.CHANGESET_MAX_LAST_EDIT_DISTANCE) {
            closeChangeset(openChangeset)
            return createChangeset(type, source, position)
        } else {
            return openChangeset.changesetId
        }
    }

    suspend fun createChangeset(type: ElementEditType, source: String, position: LatLon): Long {
        val changesetId = changesetApiClient.open(createChangesetTags(type, source))
        withContext(Dispatchers.IO) { openChangesetsDB.put(OpenChangeset(type.name, source, changesetId, position)) }
        changesetAutoCloser.enqueue(ApplicationConstants.CLOSE_CHANGESETS_AFTER_INACTIVITY_OF)
        Log.i(TAG, "Created changeset #$changesetId")
        return changesetId
    }

    suspend fun closeOldChangesets() {
        val timePassed = nowAsEpochMilliseconds() - prefs.lastEditTime
        if (timePassed < ApplicationConstants.CLOSE_CHANGESETS_AFTER_INACTIVITY_OF) return

        val openChangesets = withContext(Dispatchers.IO) { openChangesetsDB.getAll() }
        openChangesets.forEach { closeChangeset(it) }
    }

    private suspend fun closeChangeset(openChangeset: OpenChangeset) {
        try {
            changesetApiClient.close(openChangeset.changesetId)
            Log.i(TAG, "Closed changeset #${openChangeset.changesetId}")
        } catch (e: ConflictException) {
            Log.w(TAG, "Couldn't close changeset #${openChangeset.changesetId} because it has already been closed")
        } finally {
            withContext(Dispatchers.IO) { openChangesetsDB.delete(openChangeset.questType, openChangeset.source) }
        }
    }

    private fun createChangesetTags(type: ElementEditType, source: String) =
        mapOf(
            "comment" to type.changesetComment,
            "created_by" to ApplicationConstants.USER_AGENT,
            "locale" to Locale.current.toLanguageTag(),
            ApplicationConstants.QUESTTYPE_TAG_KEY to type.name,
            "source" to source
        )

    companion object {
        private const val TAG = "ChangesetManager"
    }
}
