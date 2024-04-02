package de.westnordost.streetcomplete.screens.main.edithistory

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.databinding.FragmentEditHistoryListBinding
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows a list of the edit history */
class EditHistoryFragment : Fragment(R.layout.fragment_edit_history_list) {

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
    private val viewModel by viewModel<EditHistoryViewModel>()

    private val adapter = EditHistoryAdapter(this::onSelected, this::onSelectionDeleted, this::onUndo)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialPaddingBottom = binding.editHistoryList.paddingBottom
        binding.editHistoryList.respectSystemInsets {
            updatePadding(left = it.left, top = it.top, bottom = it.bottom + initialPaddingBottom)
        }

        binding.editHistoryList.adapter = adapter

        observe(viewModel.edits) { edits ->
            adapter.setEdits(edits)
            // TODO only on first / if nothing selected yet
            val first = edits.firstOrNull { it.isUndoable }
            if (first != null) {
                adapter.select(first)
            }
            // TODO?!?! not on first...
            if (edits.isEmpty()) {
                listener?.onEditHistoryIsEmpty()
            }
        }
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
