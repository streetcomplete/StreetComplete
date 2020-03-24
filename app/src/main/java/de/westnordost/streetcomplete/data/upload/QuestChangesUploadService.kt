package de.westnordost.streetcomplete.data.upload

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

import java.util.concurrent.atomic.AtomicBoolean

import javax.inject.Inject

import de.westnordost.osmapi.common.errors.OsmAuthorizationException
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.VisibleQuestRelay
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.util.enclosingTile

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 * notes and quests he answered  */
class QuestChangesUploadService : IntentService(TAG) {
    @Inject internal lateinit var uploaders: List<Uploader>
    @Inject internal lateinit var versionIsBannedChecker: VersionIsBannedChecker
    @Inject internal lateinit var userController: UserController
    @Inject internal lateinit var downloadedTilesDB: DownloadedTilesDao
    @Inject internal lateinit var statisticsUpdater: StatisticsUpdater

    private val binder = Interface()

    // listeners
    private val visibleQuestRelay = VisibleQuestRelay()
    private val uploadedChangeRelay = object : OnUploadedChangeListener {
        override fun onUploaded(questType: String, at: LatLon) {
            statisticsUpdater.addOne(questType)
            progressListener?.onProgress(true)
        }

        override fun onDiscarded(questType: String, at: LatLon) {
            invalidateArea(at)
            progressListener?.onProgress(false)
        }
    }
    private var progressListener: QuestChangesUploadProgressListener? = null

    private val cancelState = AtomicBoolean(false)

    private val bannedInfo: BannedInfo by lazy { versionIsBannedChecker.get() }

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        cancelState.set(false)
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onDestroy() {
        cancelState.set(true)
        super.onDestroy()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (cancelState.get()) return

        progressListener?.onStarted()

        try {
            val banned = bannedInfo
            if (banned is IsBanned) {
                throw VersionBannedException(banned.reason)
            }

            // let's fail early in case of no authorization
            if (!userController.isUserAuthorized) {
                throw OsmAuthorizationException(401, "Unauthorized", "User is not authorized")
            }

            Log.i(TAG, "Starting upload")

            for (uploader in uploaders) {
                if (cancelState.get()) return
                uploader.uploadedChangeListener = uploadedChangeRelay
                uploader.visibleQuestListener = visibleQuestRelay
                uploader.upload(cancelState)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Unable to upload", e)
            progressListener?.onError(e)
        }

        progressListener?.onFinished()

        Log.i(TAG, "Finished upload")
    }

    private fun invalidateArea(pos: LatLon) {
        // called after a conflict. If there is a conflict, the user is not the only one in that
        // area, so best invalidate all downloaded quests here and redownload on next occasion
        val tile = pos.enclosingTile(ApplicationConstants.QUEST_TILE_ZOOM)
        downloadedTilesDB.remove(tile)
    }

    /** Public interface to classes that are bound to this service  */
    inner class Interface : Binder() {
        fun setProgressListener(listener: QuestChangesUploadProgressListener?) {
            progressListener = listener
        }

        fun setQuestListener(listener: VisibleQuestListener?) {
            visibleQuestRelay.listener = listener
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, QuestChangesUploadService::class.java)
        }

        private const val TAG = "Upload"
    }
}
