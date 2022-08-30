package de.westnordost.streetcomplete.data.upload

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditsUploader
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsUploader
import de.westnordost.streetcomplete.data.user.AuthorizationException
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.quests.osmose.OsmoseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class Uploader(
    private val noteEditsUploader: NoteEditsUploader,
    private val elementEditsUploader: ElementEditsUploader,
    private val downloadedTilesDB: DownloadedTilesDao,
    private val userLoginStatusSource: UserLoginStatusSource,
    private val versionIsBannedChecker: VersionIsBannedChecker,
    private val mutex: Mutex,
    private val osmoseDao: OsmoseDao,
    private val prefs: SharedPreferences,
) {
    var uploadedChangeListener: OnUploadedChangeListener? = null

    private val bannedInfo by lazy { versionIsBannedChecker.get() }

    private val uploadedChangeRelay = object : OnUploadedChangeListener {
        override fun onUploaded(questType: String, at: LatLon) {
            uploadedChangeListener?.onUploaded(questType, at)
        }

        override fun onDiscarded(questType: String, at: LatLon) {
            invalidateArea(at)
            uploadedChangeListener?.onDiscarded(questType, at)
        }
    }

    init {
        noteEditsUploader.uploadedChangeListener = uploadedChangeRelay
        elementEditsUploader.uploadedChangeListener = uploadedChangeRelay
    }

    suspend fun upload(context: Context) {
        val banned = withContext(Dispatchers.IO) { bannedInfo }
        if (banned is IsBanned) {
            throw VersionBannedException(banned.reason)
        } else if (banned is UnknownIfBanned) {
            val old = prefs.getInt(Prefs.BAN_CHECK_ERROR_COUNT, 0)
            prefs.edit().putInt(Prefs.BAN_CHECK_ERROR_COUNT, old + 1).apply()
        } else
            prefs.edit().putInt(Prefs.BAN_CHECK_ERROR_COUNT, 0).apply()
        if (prefs.getInt(Prefs.BAN_CHECK_ERROR_COUNT, 0) > 10) {
            AlertDialog.Builder(context)
                .setMessage(R.string.ban_check_fails)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        // let's fail early in case of no authorization
        if (!userLoginStatusSource.isLoggedIn) {
            throw AuthorizationException("User is not authorized")
        }

        Log.i(TAG, "Starting upload")

        mutex.withLock {
            // element edit and note edit uploader must run in sequence because the notes may need
            // to be updated if the element edit uploader creates new elements to which notes refer
            elementEditsUploader.upload(context)
            noteEditsUploader.upload()
            osmoseDao.reportChanges()
        }

        Log.i(TAG, "Finished upload")
    }

    private fun invalidateArea(pos: LatLon) {
        // called after a conflict. If there is a conflict, the user is not the only one in that
        // area, so best invalidate all downloaded quests here and redownload on next occasion
        val tile = pos.enclosingTilePos(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        downloadedTilesDB.remove(tile)
    }

    companion object {
        private const val TAG = "Upload"
    }
}
