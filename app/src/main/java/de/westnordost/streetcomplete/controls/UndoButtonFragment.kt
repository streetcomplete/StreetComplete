package de.westnordost.streetcomplete.controls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import de.westnordost.streetcomplete.data.quest.QuestController
import de.westnordost.streetcomplete.data.quest.UndoableOsmQuestsCountListener
import de.westnordost.streetcomplete.data.quest.UndoableOsmQuestsSource
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.ktx.popIn
import de.westnordost.streetcomplete.ktx.popOut
import de.westnordost.streetcomplete.quests.getHtmlQuestTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.FutureTask
import javax.inject.Inject

/** Fragment that shows (and hides) the undo button, based on whether there is anything to undo */
class UndoButtonFragment : Fragment(R.layout.fragment_undo_button),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var undoableOsmQuestsSource: UndoableOsmQuestsSource
    @Inject internal lateinit var uploadProgressSource: UploadProgressSource
    @Inject internal lateinit var questController: QuestController
    @Inject internal lateinit var featureDictionaryFutureTask: FutureTask<FeatureDictionary>

    private val undoButton get() = view as ImageButton

    /* undo button is not shown when there is nothing to undo */
    private val undoableOsmQuestsCountListener = object : UndoableOsmQuestsCountListener {
        override fun onUndoableOsmQuestsCountIncreased() {
            launch(Dispatchers.Main) {
                if (!undoButton.isVisible && undoableOsmQuestsSource.count > 0) {
                    undoButton.popIn()
                }
            }
        }

        override fun onUndoableOsmQuestsCountDecreased() {
            launch(Dispatchers.Main) {
                if (undoButton.isVisible && undoableOsmQuestsSource.count == 0) {
                    undoButton.popOut().withEndAction { undoButton.visibility = View.INVISIBLE }
                }
            }
        }
    }


    /* Don't allow undoing while uploading. Should prevent race conditions. (Undoing quest while
    *  also uploading it at the same time) */
    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { launch(Dispatchers.Main) { updateUndoButtonEnablement(false) }}
        override fun onFinished() { launch(Dispatchers.Main) { updateUndoButtonEnablement(true) }}
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        undoButton.setOnClickListener {
            undoButton.isEnabled = false
            val quest = undoableOsmQuestsSource.getLastUndoable()
            if (quest != null) confirmUndo(quest)
        }
    }

    override fun onStart() {
        super.onStart()
        updateUndoButtonVisibility()
        updateUndoButtonEnablement(true)
        undoableOsmQuestsSource.addListener(undoableOsmQuestsCountListener)
        uploadProgressSource.addUploadProgressListener(uploadProgressListener)
    }

    override fun onStop() {
        super.onStop()
        undoableOsmQuestsSource.removeListener(undoableOsmQuestsCountListener)
        uploadProgressSource.removeUploadProgressListener(uploadProgressListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun confirmUndo(quest: OsmQuest) {
        val element = questController.getOsmElement(quest) ?: return
        val ctx = context ?: return

        val inner = LayoutInflater.from(ctx).inflate(R.layout.dialog_undo, null, false)
        val icon = inner.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(quest.type.icon)
        val text = inner.findViewById<TextView>(R.id.text)
        text.text = resources.getHtmlQuestTitle(quest.type, element, featureDictionaryFutureTask)

        AlertDialog.Builder(ctx)
            .setTitle(R.string.undo_confirm_title)
            .setView(inner)
            .setPositiveButton(R.string.undo_confirm_positive) { _, _ ->
                questController.undo(quest)
                updateUndoButtonEnablement(true)
            }
            .setNegativeButton(R.string.undo_confirm_negative) { _, _ -> updateUndoButtonEnablement(true) }
            .setOnCancelListener { updateUndoButtonEnablement(true) }
            .show()
    }

    private fun updateUndoButtonVisibility() {
        view?.isGone = undoableOsmQuestsSource.count <= 0
    }

    private fun updateUndoButtonEnablement(enable: Boolean) {
        undoButton.isEnabled = enable && !uploadProgressSource.isUploadInProgress
    }
}
