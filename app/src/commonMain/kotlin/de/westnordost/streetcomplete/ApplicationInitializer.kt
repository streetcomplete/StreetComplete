package de.westnordost.streetcomplete

import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import de.westnordost.streetcomplete.util.logs.KermitLogger
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

/** Runs process-wide startup shared by Android, iOS, and desktop exactly once. */
class ApplicationInitializer(
    private val applicationScope: CoroutineScope,
    private val preloader: Preloader,
    private val cleaner: Cleaner,
    private val editHistoryController: EditHistoryController,
    private val feedsUpdater: FeedsUpdater,
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater,
    private val downloadedTilesController: DownloadedTilesController,
    private val preferences: Preferences,
    private val databaseLogger: DatabaseLogger,
) {
    private val initialized = atomic(false)

    fun initialize(schedulePeriodicCleanup: Boolean = false) {
        if (!initialized.compareAndSet(expect = false, update = true)) return

        if (Log.instances.none { it is KermitLogger }) Log.instances.add(KermitLogger())
        if (databaseLogger !in Log.instances) Log.instances.add(databaseLogger)

        applicationScope.launch {
            preloader.preload()
            cleanExpiredData()
            editHistoryController.deleteSyncedOlderThan(
                nowAsEpochMilliseconds() - ApplicationConstants.MAX_UNDO_HISTORY_AGE,
            )
        }

        if (schedulePeriodicCleanup) {
            applicationScope.launch {
                while (true) {
                    delay(1.days)
                    cleanExpiredData()
                }
            }
        }

        feedsUpdater.updateNow()
        resurveyIntervalsUpdater.update()

        val lastVersion = preferences.lastDataVersion
        if (BuildConfig.VERSION_NAME != lastVersion) {
            preferences.lastDataVersion = BuildConfig.VERSION_NAME
            if (lastVersion != null) downloadedTilesController.invalidateAll()
        }
    }

    private suspend fun cleanExpiredData() {
        try {
            cleaner.cleanOld()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unable to clean expired application data", e)
        }
    }

    private companion object {
        const val TAG = "ApplicationInitializer"
    }
}
