package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/** Fragment that shows (and hides) the undo button, based on whether there is anything to undo */
class UndoButtonFragment : Fragment(R.layout.fragment_undo_button) {

    private val editHistorySource: EditHistorySource by inject()
    private val uploadProgressSource: UploadProgressSource by inject()

    private val undoButton get() = view as ImageButton

    interface Listener {
        fun onClickShowEditHistory()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* undo button is not shown when there is nothing to undo */
    private val editHistoryListener = object : EditHistorySource.Listener {
        override fun onAdded(edit: Edit) { viewLifecycleScope.launch { animateInIfAnythingToUndo() } }
        override fun onSynced(edit: Edit) { viewLifecycleScope.launch { animateOutIfNothingLeftToUndo() } }
        override fun onDeleted(edits: List<Edit>) { viewLifecycleScope.launch { animateOutIfNothingLeftToUndo() } }
        override fun onInvalidated() { viewLifecycleScope.launch { updateUndoButtonVisibility() } }
    }

    /* Don't allow undoing while uploading. Should prevent race conditions. (Undoing quest while
     * also uploading it at the same time) */
    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { viewLifecycleScope.launch { updateUndoButtonEnablement(false) } }
        override fun onFinished() { viewLifecycleScope.launch { updateUndoButtonEnablement(true) } }
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        undoButton.setOnClickListener {
            showEditHistory()
        }
    }

    override fun onStart() {
        super.onStart()
        viewLifecycleScope.launch { updateUndoButtonVisibility() }
        updateUndoButtonEnablement(true)
        editHistorySource.addListener(editHistoryListener)
        uploadProgressSource.addUploadProgressListener(uploadProgressListener)
    }

    override fun onStop() {
        super.onStop()
        editHistorySource.removeListener(editHistoryListener)
        uploadProgressSource.removeUploadProgressListener(uploadProgressListener)
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun showEditHistory() {
        listener?.onClickShowEditHistory()
    }

    private suspend fun updateUndoButtonVisibility() {
        view?.isGone = getMostRecentUndoable() == null
    }

    private fun updateUndoButtonEnablement(enable: Boolean) {
        undoButton.isEnabled = enable && !uploadProgressSource.isUploadInProgress
    }

    private suspend fun animateInIfAnythingToUndo() {
        if (!undoButton.isVisible && getMostRecentUndoable() != null) {
            undoButton.popIn()
        }
    }

    private suspend fun animateOutIfNothingLeftToUndo() {
        if (undoButton.isVisible && getMostRecentUndoable() == null) {
            undoButton.popOut().withEndAction { undoButton.visibility = View.INVISIBLE }
        }
    }

    private suspend fun getMostRecentUndoable(): Edit? =
        withContext(Dispatchers.IO) { editHistorySource.getMostRecentUndoable() }
}
