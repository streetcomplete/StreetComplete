package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.download.DownloadProgressListener
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.prefs.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/** Fragment that shows the "star" with the number of solved quests */
class AnswersCounterFragment : Fragment(R.layout.fragment_answers_counter) {

    private val uploadProgressSource: UploadProgressSource by inject()
    private val downloadProgressSource: DownloadProgressSource by inject()

    private val prefs: Preferences by inject()
    private val statisticsSource: StatisticsSource by inject()
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource by inject()

    private val answersCounterView get() = view as AnswersCounterView
    private var showCurrentWeek: Boolean = false

    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { viewLifecycleScope.launch { updateProgress() } }
        override fun onFinished() { viewLifecycleScope.launch { updateProgress() } }
    }

    private val downloadProgressListener = object : DownloadProgressListener {
        override fun onStarted() { viewLifecycleScope.launch { updateProgress() } }
        override fun onFinished() { viewLifecycleScope.launch { updateProgress() } }
    }

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { viewLifecycleScope.launch { updateCount(true) } }
        override fun onDecreased() { viewLifecycleScope.launch { updateCount(true) } }
    }

    private val statisticsListener = object : StatisticsSource.Listener {
        override fun onAddedOne(type: String) {
            viewLifecycleScope.launch { addCount(+1, true) }
        }
        override fun onSubtractedOne(type: String) {
            viewLifecycleScope.launch { addCount(-1, true) }
        }
        override fun onUpdatedAll() {
            viewLifecycleScope.launch { updateCount(false) }
        }
        override fun onCleared() {
            viewLifecycleScope.launch { updateCount(false) }
        }

        override fun onUpdatedDaysActive() {}
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showCurrentWeek = savedInstanceState?.getBoolean(SHOW_CURRENT_WEEK, false) ?: false
        answersCounterView.showLabel = showCurrentWeek

        answersCounterView.setOnClickListener {
            showCurrentWeek = !showCurrentWeek
            viewLifecycleScope.launch {
                updateCount(false)
                answersCounterView.showLabel = showCurrentWeek
            }
        }
    }

    override fun onStart() {
        super.onStart()

        updateProgress()
        uploadProgressSource.addUploadProgressListener(uploadProgressListener)
        downloadProgressSource.addDownloadProgressListener(downloadProgressListener)
        // If autosync is on, the answers counter shows the uploaded + uploadable amount of quests.
        if (isAutosync) unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)
        statisticsSource.addListener(statisticsListener)

        viewLifecycleScope.launch { updateCount(false) }
    }

    override fun onStop() {
        super.onStop()
        uploadProgressSource.removeUploadProgressListener(uploadProgressListener)
        downloadProgressSource.removeDownloadProgressListener(downloadProgressListener)
        statisticsSource.removeListener(statisticsListener)
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_CURRENT_WEEK, showCurrentWeek)
    }

    private val isAutosync: Boolean get() =
        Prefs.Autosync.valueOf(prefs.getStringOrNull(Prefs.AUTOSYNC) ?: "ON") == Prefs.Autosync.ON

    private fun updateProgress() {
        answersCounterView.showProgress =
            uploadProgressSource.isUploadInProgress || downloadProgressSource.isDownloadInProgress
    }

    private suspend fun updateCount(animated: Boolean) {
        /* if autosync is on, show the uploaded count + the to-be-uploaded count (but only those
           uploadables that will be part of the statistics, so no note stuff) */
        val editCount = withContext(Dispatchers.IO) {
            if (showCurrentWeek) statisticsSource.getCurrentWeekEditCount() else statisticsSource.getEditCount()
        }
        val amount = editCount + if (isAutosync) unsyncedChangesCountSource.getSolvedCount() else 0
        answersCounterView.setUploadedCount(amount, animated)
    }

    private fun addCount(diff: Int, animate: Boolean) {
        answersCounterView.setUploadedCount(answersCounterView.uploadedCount + diff, animate)
    }

    companion object {
        private const val SHOW_CURRENT_WEEK = "showCurrentWeek"
    }
}
