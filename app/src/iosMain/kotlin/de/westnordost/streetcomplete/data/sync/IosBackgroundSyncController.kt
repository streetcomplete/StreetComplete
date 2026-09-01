package de.westnordost.streetcomplete.data.sync

import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.connection.ActiveNetworkConnection
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.preferences.Autosync
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/** Runs the network work that remains valid without foreground location authorization.
 *
 * TODO(multiplatform): Add background map downloads only if StreetComplete adopts an Apple-
 * approved background-location mode. Its current when-in-use authorization cannot provide a
 * current download position after iOS suspends the foreground scene.
 */
class IosBackgroundSyncController(
    private val applicationScope: CoroutineScope,
    private val uploader: Uploader,
    private val openChangesetsManager: OpenChangesetsManager,
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
    private val userLoginSource: UserLoginSource,
    private val activeNetworkConnection: ActiveNetworkConnection,
    private val preferences: Preferences,
) {
    fun start(completion: (Boolean) -> Unit): IosBackgroundSyncHandle {
        val job = applicationScope.launch {
            val success = try {
                runSync()
            } catch (_: CancellationException) {
                false
            } catch (error: Exception) {
                Log.e(TAG, "iOS background sync failed", error)
                false
            }
            completion(success)
        }
        return IosBackgroundSyncHandle(job)
    }

    private suspend fun runSync(): Boolean {
        if (!userLoginSource.isLoggedIn) return true

        var success = true
        if (isAutoUploadAllowed() && unsyncedChangesCountSource.getCount() > 0) {
            try {
                uploader.upload()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(TAG, "Unable to upload during iOS background processing", error)
                success = false
            }
        }
        // Uploader deliberately reports several failures through its own status flow. Re-check
        // cancellation here so an iOS expiration cannot be mistaken for a successful task when
        // the upload path returns without propagating CancellationException.
        currentCoroutineContext().ensureActive()

        try {
            openChangesetsManager.closeOldChangesets()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.e(TAG, "Unable to close changesets during iOS background processing", error)
            success = false
        }
        return success
    }

    private suspend fun isAutoUploadAllowed(): Boolean = when (preferences.autosync) {
        Autosync.ON -> true
        Autosync.WIFI -> {
            val capabilities = activeNetworkConnection.capabilities ?: withTimeoutOrNull(5.seconds) {
                activeNetworkConnection.capabilitiesFlow.filterNotNull().first()
            }
            capabilities?.hasInternet == true && !capabilities.isMetered
        }
        Autosync.OFF -> false
    }

    private companion object {
        const val TAG = "IosBackgroundSync"
    }
}

class IosBackgroundSyncHandle internal constructor(private val job: Job) {
    fun cancel() = job.cancel()
}
