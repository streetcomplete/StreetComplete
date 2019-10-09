package de.westnordost.streetcomplete.data.upload

import android.app.IntentService
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

import javax.inject.Inject

import de.westnordost.osmapi.common.errors.OsmAuthorizationException
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.VisibleQuestRelay
import de.westnordost.streetcomplete.data.osm.upload.OsmQuestsUpload
import de.westnordost.streetcomplete.data.osm.upload.SplitWaysUpload
import de.westnordost.streetcomplete.data.osm.upload.UndoOsmQuestsUpload
import de.westnordost.streetcomplete.data.osmnotes.CreateNotesUpload
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestsChangesUpload
import de.westnordost.streetcomplete.oauth.OAuthPrefs

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 * notes and quests he answered  */
class QuestChangesUploadService : IntentService(TAG) {

    @Inject internal lateinit var noteQuestUpload: OsmNoteQuestsChangesUpload
    @Inject internal lateinit var questUpload: OsmQuestsUpload
    @Inject internal lateinit var undoQuestUpload: UndoOsmQuestsUpload
    @Inject internal lateinit var createNoteUpload: CreateNotesUpload
    @Inject internal lateinit var splitWaysUpload: SplitWaysUpload
    @Inject internal lateinit var oAuth: OAuthPrefs

	/** Public interface to classes that are bound to this service  */
	inner class Interface : Binder() {
		fun setProgressListener(listener: QuestChangesUploadProgressListener) {
			progressListener = listener
		}

		fun setQuestListener(listener: VisibleQuestListener) {
			visibleQuestRelay.listener = listener
		}
	}
    private val binder = Interface()

    // listeners
    private val visibleQuestRelay = VisibleQuestRelay()
    private val uploadedChangeRelay = object : OnUploadedChangeListener {
        override fun onUploaded() {
            progressListener?.onProgress(true)
        }

        override fun onDiscarded() {
            progressListener?.onProgress(false)
        }
    }
	private var progressListener: QuestChangesUploadProgressListener? = null

	private val cancelState = AtomicBoolean(false)

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
            if (isBanned) {
                throw VersionBannedException(banReason)
            }

            // let's fail early in case of no authorization
            if (!oAuth.isAuthorized) {
                throw OsmAuthorizationException(401, "Unauthorized", "User is not authorized")
            }

            Log.i(TAG, "Starting upload")

            noteQuestUpload.uploadedChangeListener = uploadedChangeRelay
            noteQuestUpload.upload(cancelState)

            if (cancelState.get()) return

            undoQuestUpload.uploadedChangeListener = uploadedChangeRelay
            undoQuestUpload.visibleQuestListener = visibleQuestRelay
            undoQuestUpload.upload(cancelState)

            if (cancelState.get()) return

            questUpload.uploadedChangeListener = uploadedChangeRelay
            questUpload.visibleQuestListener = visibleQuestRelay
            questUpload.upload(cancelState)

            if (cancelState.get()) return

            splitWaysUpload.uploadedChangeListener = uploadedChangeRelay
            splitWaysUpload.visibleQuestListener = visibleQuestRelay
            splitWaysUpload.upload(cancelState)

            if (cancelState.get()) return

            createNoteUpload.uploadedChangeListener = uploadedChangeRelay
            createNoteUpload.upload(cancelState)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to upload", e)
            progressListener?.onError(e)
        }

	    progressListener?.onFinished()

        Log.i(TAG, "Finished upload")
    }

    companion object {
        private val TAG = "Upload"

        private var banReason: String? = null
        private val isBanned: Boolean by lazy { checkBanned() }

        private fun checkBanned(): Boolean {
	        var connection: HttpURLConnection? = null
            try {
                val url = URL("https://www.westnordost.de/streetcomplete/banned_versions.txt")
	            connection = (url.openConnection() as HttpURLConnection)
	            connection.inputStream.bufferedReader().use { reader ->
		            for (line in reader.lineSequence()) {
			            val text = line.split("\t".toRegex())
			            val userAgent = text[0]
			            if (userAgent == ApplicationConstants.USER_AGENT) {
				            banReason = if (text.size > 1) text[1] else null
				            return true
			            }
		            }
	            }
            } catch (e: IOException) {
                // if there is an io exception, never mind then...! (The unreachability of the above
                // internet address should not lead to this app being unusable!)
            } finally {
                connection?.disconnect()
            }
            return false
        }
    }
}
