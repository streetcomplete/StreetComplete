package de.westnordost.streetcomplete.screens.main.edithistory

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.databinding.FragmentEditHistoryListBinding
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/** Shows a list of the edit history */
class EditHistoryFragment(private val allHidden: Boolean = false) : Fragment(R.layout.fragment_edit_history_list) {

    private val editHistorySource: EditHistorySource by inject()
    private val prefs: ObservableSettings by inject()

    interface Listener {
        /** Called when an edit has been selected and the undo-button appeared */
        fun onSelectedEdit(edit: Edit)
        /** Called when the edit that was selected has been removed */
        fun onDeletedSelectedEdit()
        /** Called when the edit history is empty now */
        fun onEditHistoryIsEmpty()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val binding by viewBinding(FragmentEditHistoryListBinding::bind)

    private val adapter = EditHistoryAdapter(this::onSelected, this::onSelectionDeleted, this::onUndo)

    private val editHistoryListener = object : EditHistorySource.Listener {
        override fun onAdded(edit: Edit) { viewLifecycleScope.launch { adapter.onAdded(edit) } }
        override fun onSynced(edit: Edit) { viewLifecycleScope.launch { adapter.onSynced(edit) } }

        override fun onDeleted(edits: List<Edit>) {
            viewLifecycleScope.launch {
                adapter.onDeleted(edits)
                if (editHistorySource.getCount() == 0) {
                    listener?.onEditHistoryIsEmpty()
                }
            }
        }

        override fun onInvalidated() {
            viewLifecycleScope.launch {
                val edits = withContext(Dispatchers.IO) { editHistorySource.getAll() }
                adapter.setEdits(edits)
                if (edits.isEmpty()) {
                    listener?.onEditHistoryIsEmpty()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialPaddingBottom = binding.editHistoryList.paddingBottom
        binding.editHistoryList.respectSystemInsets {
            updatePadding(left = it.left, top = it.top, bottom = it.bottom + initialPaddingBottom)
        }
        viewLifecycleScope.launch {
            val edits = withContext(Dispatchers.IO) { editHistorySource.getAll(allHidden) }
            adapter.setEdits(edits)
            val first = edits.firstOrNull { it.isUndoable }
            if (first != null && prefs.getBoolean(Prefs.SELECT_FIRST_EDIT, true)) {
                adapter.select(first)
            }
            binding.editHistoryList.adapter = adapter
            editHistorySource.addListener(editHistoryListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editHistorySource.removeListener(editHistoryListener)
    }

    fun select(editKey: EditKey) {
        val edit = editHistorySource.get(editKey) ?: return
        adapter.select(edit)
    }

    private fun onSelected(edit: Edit) {
        listener?.onSelectedEdit(edit)
    }

    private fun onSelectionDeleted() {
        listener?.onDeletedSelectedEdit()
    }

    private fun onUndo(edit: Edit) {
        UndoDialog(requireContext(), edit).show()
    }
}
