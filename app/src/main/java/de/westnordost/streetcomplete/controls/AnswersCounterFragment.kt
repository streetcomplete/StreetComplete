package de.westnordost.streetcomplete.controls

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.UnsyncedChangesCountListener
import de.westnordost.streetcomplete.data.quest.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Fragment that shows (and hides) the undo button, based on whether there is anything to undo */
class AnswersCounterFragment : Fragment(R.layout.fragment_answers_counter),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var uploadProgressSource: UploadProgressSource
    @Inject internal lateinit var prefs: SharedPreferences
    @Inject internal lateinit var questStatisticsDao: QuestStatisticsDao
    @Inject internal lateinit var unsyncedChangesCountSource: UnsyncedChangesCountSource

    private val answersCounterView get() = view as AnswersCounterView

    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { launch(Dispatchers.Main) { updateProgress(true) } }
        override fun onFinished() { launch(Dispatchers.Main) { updateProgress(false) } }
    }

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountListener {
        override fun onUnsyncedChangesCountIncreased() { launch(Dispatchers.Main) { updateCount(true) }}
        override fun onUnsyncedChangesCountDecreased() { launch(Dispatchers.Main) { updateCount(true) }}
    }

    private val questStatisticsListener = object : QuestStatisticsDao.Listener {
        override fun onAddedOne(questType: String) {
            launch(Dispatchers.Main) {
                answersCounterView.setUploadedCount(answersCounterView.uploadedCount + 1, true)
            }
        }
        override fun onSubtractedOne(questType: String) {
            launch(Dispatchers.Main) {
                launch(Dispatchers.Main) {
                    answersCounterView.setUploadedCount(answersCounterView.uploadedCount - 1, true)
                }
            }
        }
        override fun onReplacedAll() {
            launch(Dispatchers.Main) { updateCount(false) }
        }
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
        updateCount(false)
        if (isAutosync) {
            uploadProgressSource.addUploadProgressListener(uploadProgressListener)
            unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)
        }
        questStatisticsDao.addListener(questStatisticsListener)
    }

    override fun onStop() {
        super.onStop()
        uploadProgressSource.removeUploadProgressListener(uploadProgressListener)
        questStatisticsDao.removeListener(questStatisticsListener)
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    private val isAutosync: Boolean get() =
        Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) == Prefs.Autosync.ON

    private fun updateProgress(isUploadInProgress: Boolean) {
        answersCounterView.showProgress = isUploadInProgress && isAutosync
    }

    private fun updateCount(animated: Boolean) {
        /* if autosync is on, show the uploaded count + the to-be-uploaded count (but only those
           uploadables that will be part of the statistics, so no note stuff) */
        val amount = questStatisticsDao.getTotalAmount() + if (isAutosync) unsyncedChangesCountSource.questCount else 0
        answersCounterView.setUploadedCount(amount, animated)
    }
}