package de.westnordost.streetcomplete.data.upload

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditsUploader
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsUploader
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class Uploader(
    private val noteEditsUploader: NoteEditsUploader,
    private val elementEditsUploader: ElementEditsUploader,
    private val downloadedTilesController: DownloadedTilesController,
    private val userLoginSource: UserLoginSource,
    private val versionIsBannedChecker: VersionIsBannedChecker,
    private val mutex: Mutex,
    private val externalSourceQuestController: ExternalSourceQuestController,
    private val prefs: ObservableSettings,
) : UploadProgressSource {

    private val listeners = Listeners<UploadProgressSource.Listener>()

    private lateinit var bannedInfo: BannedInfo

    private val uploadedChangeRelay = object : OnUploadedChangeListener {
        override fun onUploaded(questType: String, at: LatLon) {
            listeners.forEach { it.onUploaded(questType, at) }
        }

        override fun onDiscarded(questType: String, at: LatLon) {
            invalidateArea(at)
            listeners.forEach { it.onDiscarded(questType, at) }
        }
    }

    init {
        noteEditsUploader.uploadedChangeListener = uploadedChangeRelay
        elementEditsUploader.uploadedChangeListener = uploadedChangeRelay
    }

    override var isUploadInProgress: Boolean = false
        private set

    suspend fun upload() {
        try {
            isUploadInProgress = true
            listeners.forEach { it.onStarted() }

            if (!::bannedInfo.isInitialized) {
                bannedInfo = withContext(Dispatchers.IO) { versionIsBannedChecker.get() }
            }
            val banned = bannedInfo
            if (banned is IsBanned) {
                throw VersionBannedException(banned.reason)
            } else if (banned is UnknownIfBanned) {
                val old = prefs.getInt(Prefs.BAN_CHECK_ERROR_COUNT, 0)
                prefs.putInt(Prefs.BAN_CHECK_ERROR_COUNT, old + 1)
            } else
                prefs.putInt(Prefs.BAN_CHECK_ERROR_COUNT, 0)
            if (prefs.getInt(Prefs.BAN_CHECK_ERROR_COUNT, 0) > 10) {
                // todo: make it work again, or kick it out...
//                ContextCompat.getMainExecutor(context).execute {
//                    context.toast(R.string.ban_check_fails, Toast.LENGTH_LONG)
//                }
            }

            // let's fail early in case of no authorization
            if (!userLoginSource.isLoggedIn && !BuildConfig.DEBUG) {
                throw AuthorizationException("User is not authorized")
            }

            Log.i(TAG, "Starting upload")

            mutex.withLock {
                // element edit and note edit uploader must run in sequence because the notes may need
                // to be updated if the element edit uploader creates new elements to which notes refer
                elementEditsUploader.upload(this)
                if (!userLoginSource.isLoggedIn) return@withLock // avoid the 2 below in debug apk
                noteEditsUploader.upload()
                externalSourceQuestController.upload()
            }
            Log.i(TAG, "Finished upload")
        } catch (e: CancellationException) {
            Log.i(TAG, "Upload cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Unable to upload", e)
            listeners.forEach { it.onError(e) }
            throw e
        } finally {
            isUploadInProgress = false
            listeners.forEach { it.onFinished() }
        }
    }

    override fun addListener(listener: UploadProgressSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UploadProgressSource.Listener) {
        listeners.remove(listener)
    }

    private fun invalidateArea(pos: LatLon) {
        // called after a conflict. If there is a conflict, the user is not the only one in that
        // area, so best invalidate all downloaded quests here and redownload on next occasion
        val tile = pos.enclosingTilePos(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        downloadedTilesController.invalidate(tile)
    }

    companion object {
        const val TAG = "Upload"
    }
}
