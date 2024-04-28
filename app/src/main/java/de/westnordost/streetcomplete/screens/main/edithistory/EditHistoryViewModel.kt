package de.westnordost.streetcomplete.screens.main.edithistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistoryController
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.ktx.toLocalDateTime
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

abstract class EditHistoryViewModel : ViewModel() {
    abstract val editItems: StateFlow<List<EditItem>>
    abstract val selectedEdit: StateFlow<Edit?>

    abstract suspend fun getEditElement(edit: Edit): Element?
    abstract suspend fun getEditGeometry(edit: Edit): ElementGeometry

    abstract fun select(editKey: EditKey?)
    abstract fun undo(editKey: EditKey)

    abstract val featureDictionaryLazy: Lazy<FeatureDictionary>
}

data class EditItem(
    val edit: Edit,
    val showDate: Boolean,
    val showTime: Boolean,
    val isSelected: Boolean
)

class EditHistoryViewModelImpl(
    private val mapDataSource: MapDataWithEditsSource,
    private val editHistoryController: EditHistoryController,
    override val featureDictionaryLazy: Lazy<FeatureDictionary>,
) : EditHistoryViewModel() {

    private val edits = MutableStateFlow<List<Edit>>(emptyList())

    override val selectedEdit = MutableStateFlow<Edit?>(null)

    override val editItems = combine(edits, selectedEdit) { edits, selection ->
        edits.mapIndexed { index, edit ->
            val editAbove = if (index < edits.indices.last) edits[index + 1] else null

            val editDateTime = Instant.fromEpochMilliseconds(edit.createdTimestamp).toLocalDateTime()
            val editAboveDateTime = editAbove?.let { Instant.fromEpochMilliseconds(it.createdTimestamp).toLocalDateTime() }
            val sameDate = editDateTime.date == editAboveDateTime?.date
            val sameTime = editDateTime.time.hour == editAboveDateTime?.time?.hour &&
                           editDateTime.time.minute == editAboveDateTime.time.minute

            EditItem(edit,
                showDate = !sameDate,
                showTime = !sameTime || !sameDate,
                isSelected = selection?.key == edit.key
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override suspend fun getEditElement(edit: Edit): Element? {
        val key = edit.primaryElementKey ?: return null
        return withContext(IO) { mapDataSource.get(key.type, key.id) }
    }

    override suspend fun getEditGeometry(edit: Edit): ElementGeometry = when (edit) {
        is ElementEdit -> edit.originalGeometry
        is OsmQuestHidden -> withContext(IO) { mapDataSource.getGeometry(edit.elementType, edit.elementId) }
        else -> null
    } ?: ElementPointGeometry(edit.position)

    override fun select(editKey: EditKey?) {
        selectedEdit.value =
            if (editKey != null) {
                edits.value.firstOrNull { it.key == editKey }
            } else {
                null
            }
    }

    override fun undo(editKey: EditKey) {
        launch(IO) {
            editHistoryController.undo(editKey)
        }
    }

    private val editHistoryListener = object : EditHistorySource.Listener {
        override fun onAdded(added: Edit) {
            edits.update { edits ->
                var insertIndex = edits.indexOfFirst { it.createdTimestamp < added.createdTimestamp }
                if (insertIndex == -1) insertIndex = edits.size
                edits.toMutableList().also { it.add(insertIndex, added) }
            }
        }

        override fun onSynced(synced: Edit) {
            if (selectedEdit.value?.key == synced.key) {
                selectedEdit.value = synced
            }
            edits.update { edits ->
                val editIndex = edits.indexOfFirst { it.key == synced.key }
                if (editIndex != -1) {
                    edits.toMutableList().also { it[editIndex] = synced }
                } else {
                    edits
                }
            }
        }

        override fun onDeleted(deleted: List<Edit>) {
            val deletedKeys = deleted.mapTo(HashSet()) { it.key }
            if (selectedEdit.value?.key in deletedKeys) {
                selectedEdit.value = null
            }
            edits.update { edits ->
                edits.filter { it.key !in deletedKeys }
            }
        }

        override fun onInvalidated() {
            updateEdits()
        }
    }

    init {
        updateEdits()
        editHistoryController.addListener(editHistoryListener)
    }

    override fun onCleared() {
        editHistoryController.removeListener(editHistoryListener)
    }

    private fun updateEdits() {
        launch(IO) {
            edits.value = editHistoryController.getAll().sortedByDescending { it.createdTimestamp }
        }
    }
}

private val Edit.primaryElementKey: ElementKey? get() = when (this) {
    is ElementEdit -> action.elementKeys.firstOrNull()
    is OsmQuestHidden -> ElementKey(elementType, elementId)
    else -> null
}
