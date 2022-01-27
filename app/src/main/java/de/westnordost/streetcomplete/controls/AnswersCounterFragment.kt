package de.westnordost.streetcomplete.controls

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Fragment that shows the "star" with the number of solved quests */
class AnswersCounterFragment : Fragment(R.layout.fragment_answers_counter) {

    @Inject internal lateinit var uploadProgressSource: UploadProgressSource
    @Inject internal lateinit var prefs: SharedPreferences
    @Inject internal lateinit var statisticsSource: StatisticsSource
    @Inject internal lateinit var unsyncedChangesCountSource: UnsyncedChangesCountSource

    private val answersCounterView get() = view as AnswersCounterView

    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { viewLifecycleScope.launch { updateProgress(true) } }
        override fun onFinished() { viewLifecycleScope.launch { updateProgress(false) } }
    }

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { viewLifecycleScope.launch { updateCount(true) }}
        override fun onDecreased() { viewLifecycleScope.launch { updateCount(true) }}
    }

    private val questStatisticsListener = object : StatisticsSource.Listener {
        override fun onAddedOne(questType: QuestType<*>) {
            viewLifecycleScope.launch { addCount(+1,true) }
        }
        override fun onSubtractedOne(questType: QuestType<*>) {
            viewLifecycleScope.launch { addCount(-1,true) }
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

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onStart() {
        super.onStart()
        /* If autosync is on, the answers counter also shows the upload progress bar instead of
         *  upload button, and shows the uploaded + uploadable amount of quests.
         */
        updateProgress(uploadProgressSource.isUploadInProgress)
        viewLifecycleScope.launch { updateCount(false) }
        if (isAutosync) {
            uploadProgressSource.addUploadProgressListener(uploadProgressListener)
            unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)
        }
        statisticsSource.addListener(questStatisticsListener)
    }

    override fun onStop() {
        super.onStop()
        uploadProgressSource.removeUploadProgressListener(uploadProgressListener)
        statisticsSource.removeListener(questStatisticsListener)
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
    }

    private val isAutosync: Boolean get() =
        Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) == Prefs.Autosync.ON

    private fun updateProgress(isUploadInProgress: Boolean) {
        answersCounterView.showProgress = isUploadInProgress && isAutosync
    }

    private suspend fun updateCount(animated: Boolean) {
        /* if autosync is on, show the uploaded count + the to-be-uploaded count (but only those
           uploadables that will be part of the statistics, so no note stuff) */
        val amount = statisticsSource.getSolvedCount() + if (isAutosync) unsyncedChangesCountSource.getSolvedCount() else 0
        answersCounterView.setUploadedCount(amount, animated)
    }

    private fun addCount(diff: Int, animate: Boolean) {
        answersCounterView.setUploadedCount(answersCounterView.uploadedCount + diff, animate)
    }
}
