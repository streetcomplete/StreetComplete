package de.westnordost.streetcomplete.data.upload

import android.util.Log
import de.westnordost.osmapi.common.errors.OsmAuthorizationException
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditsUploader
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsUploader
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.util.enclosingTilePos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Uploader @Inject constructor(
    private val noteEditsUploader: NoteEditsUploader,
    private val elementEditsUploader: ElementEditsUploader,
    private val downloadedTilesDB: DownloadedTilesDao,
    private val userController: UserController,
    private val versionIsBannedChecker: VersionIsBannedChecker
) {
    var uploadedChangeListener: OnUploadedChangeListener? = null

    private val bannedInfo by lazy { versionIsBannedChecker.get()  }

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

    suspend fun upload() {
        val banned = withContext(Dispatchers.IO) { bannedInfo }
        if (banned is IsBanned) {
            throw VersionBannedException(banned.reason)
        }

        // let's fail early in case of no authorization
        if (!userController.isLoggedIn) {
            throw OsmAuthorizationException(401, "Unauthorized", "User is not authorized")
        }

        Log.i(TAG, "Starting upload")

        coroutineScope {
            // uploaders can run concurrently
            launch { noteEditsUploader.upload() }
            launch { elementEditsUploader.upload() }
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
