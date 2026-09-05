package de.westnordost.streetcomplete.data.sync

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.data.download.Downloader
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.ChangesetAutoCloser
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.upload.UploadController
import de.westnordost.streetcomplete.data.upload.Uploader
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Runs unique sync work in a caller-owned application scope. */
class CoroutineUploadController(
    private val scope: CoroutineScope,
    private val uploadAction: suspend () -> Unit,
) : UploadController {

    constructor(scope: CoroutineScope, uploader: Uploader) : this(scope, uploader::upload)

    private val lock = ReentrantLock()
    private var job: Job? = null

    override fun upload(isUserInitiated: Boolean) {
        lock.withLock {
            if (job?.isActive == true) return
            job = scope.launch {
                try {
                    uploadAction()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Uploader has already published the failure through UploadProgressSource.
                    // A foreground sync failure must not escape this root application coroutine.
                }
            }
        }
    }
}

/**
 * Runs one download at a time. User-initiated downloads replace an active automatic download;
 * automatic downloads keep the already-running work.
 */
class CoroutineDownloadController(
    private val scope: CoroutineScope,
    private val downloadAction: suspend (BoundingBox, Boolean) -> Unit,
) : DownloadController {

    constructor(scope: CoroutineScope, downloader: Downloader) :
        this(scope, downloader::download)

    private val lock = ReentrantLock()
    private var job: Job? = null

    override fun download(bbox: BoundingBox, isUserInitiated: Boolean) {
        lock.withLock {
            if (job?.isActive == true && !isUserInitiated) return
            job?.cancel()
            job = scope.launch {
                try {
                    downloadAction(bbox, isUserInitiated)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Downloader has already published the failure through DownloadProgressSource.
                    // A foreground sync failure must not escape this root application coroutine.
                }
            }
        }
    }
}

/** Replaces a pending close with a newly delayed close after each changeset edit. */
class CoroutineChangesetAutoCloser(
    private val scope: CoroutineScope,
    private val closeOldChangesetsAction: suspend () -> Unit,
) : ChangesetAutoCloser {

    constructor(scope: CoroutineScope, manager: OpenChangesetsManager) :
        this(scope, manager::closeOldChangesets)

    private val lock = ReentrantLock()
    private var job: Job? = null

    override fun enqueue(delayInMilliseconds: Long) {
        lock.withLock {
            job?.cancel()
            job = scope.launch {
                delay(delayInMilliseconds)
                try {
                    closeOldChangesetsAction()
                } catch (_: ConnectionException) {
                    // The OSM API closes abandoned changesets after one hour.
                } catch (_: AuthorizationException) {
                    // Reauthentication is handled when the user next opens the app.
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to close old changesets", e)
                }
            }
        }
    }

    private companion object {
        const val TAG = "ChangesetAutoCloser"
    }
}
