package de.westnordost.streetcomplete.screens.main.edithistory

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import java.text.DateFormat

abstract class EditHistoryViewModel : ViewModel() {
    abstract val edits: StateFlow<List<EditHistoryItem>>

    abstract val selectedEdit: StateFlow<Edit?>
}

sealed interface EditHistoryItem {
    data class EditItem(val edit: Edit, val isSelected: Boolean) : EditHistoryItem
    data object SyncedHeader : EditHistoryItem
    data class DateHeader(val date: LocalDate) : EditHistoryItem
}

class EditHistoryViewModelImpl(
    private val editHistorySource: EditHistorySource
) : EditHistoryViewModel() {

    override val edits = MutableStateFlow<List<EditHistoryItem>>(emptyList())

    override val selectedEdit = MutableStateFlow<Edit?>(null)

    private val editHistoryListener = object : EditHistorySource.Listener {
        override fun onAdded(added: Edit) {
            edits.update { edits ->
                var insertIndex = edits.indexOfFirst { it.createdTimestamp < added.createdTimestamp }
                if (insertIndex == -1) insertIndex = edits.size
                edits.toMutableList().also { it.add(insertIndex, added) }
            }
        }

        override fun onSynced(synced: Edit) {
            edits.update { edits ->
                val editIndex = edits.indexOfFirst { it.key == synced.key }
                if (editIndex != -1) edits.toMutableList().also { it[editIndex] = synced }
                else edits
            }
        }

        override fun onDeleted(deleted: List<Edit>) {
            val deletedKeys = deleted.mapTo(HashSet()) { it.key }
            if (selectedEdit.value?.key in deletedKeys) {
                selectedEdit.value = null
            }
            edits.update { edits ->
                edits.filter { it.key in deletedKeys }
            }
        }

        override fun onInvalidated() {
            updateEdits()
        }
    }

    init {
        editHistorySource.addListener(editHistoryListener)
    }

    override fun onCleared() {
        editHistorySource.removeListener(editHistoryListener)
    }

    private fun updateEdits() {
        launch(IO) {
            edits.value = editHistorySource.getAll().sortedByDescending { it.createdTimestamp }
            // only if there is no selection yet
            selectedEdit.compareAndSet(null, edits.value.firstOrNull { it.isUndoable })
        }
    }
}

private fun Edit.formatSameDayTime() = DateUtils.formatSameDayTime(
    createdTimestamp, nowAsEpochMilliseconds(), DateFormat.SHORT, DateFormat.SHORT
)

private fun Edit.formatDate() = DateFormat.getDateInstance(DateFormat.SHORT).format(createdTimestamp)

private val Edit.isToday: Boolean get() = DateUtils.isToday(this.createdTimestamp)
