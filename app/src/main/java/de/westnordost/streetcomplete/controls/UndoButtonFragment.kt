package de.westnordost.streetcomplete.controls

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.edithistory.UndoDialog
import de.westnordost.streetcomplete.ktx.popIn
import de.westnordost.streetcomplete.ktx.popOut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Fragment that shows (and hides) the undo button, based on whether there is anything to undo */
class UndoButtonFragment : Fragment(R.layout.fragment_undo_button) {

    @Inject internal lateinit var editHistorySource: EditHistorySource
    @Inject internal lateinit var uploadProgressSource: UploadProgressSource

    private val undoButton get() = view as ImageButton

    /* undo button is not shown when there is nothing to undo */
    private val editHistoryListener = object : EditHistorySource.Listener {
        override fun onAdded(edit: Edit) { lifecycleScope.launch { animateInIfAnythingToUndo() }}
        override fun onSynced(edit: Edit) { lifecycleScope.launch { animateOutIfNothingLeftToUndo() }}
        override fun onDeleted(edit: Edit) { lifecycleScope.launch { animateOutIfNothingLeftToUndo() }}
    }

    /* Don't allow undoing while uploading. Should prevent race conditions. (Undoing quest while
    *  also uploading it at the same time) */
    private val uploadProgressListener = object : UploadProgressListener {
        override fun onStarted() { lifecycleScope.launch { updateUndoButtonEnablement(false) }}
        override fun onFinished() { lifecycleScope.launch { updateUndoButtonEnablement(true) }}
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        undoButton.setOnClickListener {
            undoButton.isEnabled = false
            lifecycleScope.launch { confirmUndo() }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch { updateUndoButtonVisibility() }
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

    private suspend fun confirmUndo() {
        val edit = getMostRecentUndoable() ?: return
        val dlg = UndoDialog(requireContext(), edit)
        dlg.setOnDismissListener { undoButton.isEnabled = true }
        dlg.show()
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
