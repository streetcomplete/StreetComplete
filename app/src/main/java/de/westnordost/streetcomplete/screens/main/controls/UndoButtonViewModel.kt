package de.westnordost.streetcomplete.screens.main.controls

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class UndoButtonViewModel : ViewModel() {
    abstract val isUploadInProgress: StateFlow<Boolean>
    abstract val hasUndoableEdits: StateFlow<Boolean>
}

class UndoButtonViewModelImpl(
    private val editHistorySource: EditHistorySource,
    private val uploadProgressSource: UploadProgressSource,
) : UndoButtonViewModel() {
    override val isUploadInProgress = MutableStateFlow(false)
    override val hasUndoableEdits = MutableStateFlow(false)
    private var undoable: Edit? = null // not necessarily the last

    private val editHistoryListener = object : EditHistorySource.Listener {
        override fun onAdded(edit: Edit) {
            if (undoable == null && edit.isUndoable) {
                undoable = edit
                hasUndoableEdits.value = true
            }
        }
        override fun onSynced(edit: Edit) {
            val undoable = undoable
            if (undoable == null || undoable.key == edit.key) {
                updateMostRecentUndoable()
            }
        }
        override fun onDeleted(edits: List<Edit>) {
            val undoable = undoable
            if (undoable == null || undoable.key in edits.map { it.key }) {
                updateMostRecentUndoable()
            }
        }
        override fun onInvalidated() { updateMostRecentUndoable() }
    }

    private val uploadProgressListener = object : UploadProgressSource.Listener {
        override fun onStarted() { isUploadInProgress.value = true }
        override fun onFinished() { isUploadInProgress.value = false }
    }

    init {
        editHistorySource.addListener(editHistoryListener)
        uploadProgressSource.addListener(uploadProgressListener)
    }

    override fun onCleared() {
        editHistorySource.removeListener(editHistoryListener)
        uploadProgressSource.removeListener(uploadProgressListener)
    }

    private fun updateMostRecentUndoable() {
        launch(IO) {
            undoable = editHistorySource.getMostRecentUndoable()
            hasUndoableEdits.value = undoable != null
        }
    }
}
