package de.westnordost.streetcomplete.screens.main.edithistory

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.databinding.FragmentEditHistoryListBinding
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows a list of the edit history */
class EditHistoryFragment : Fragment(R.layout.fragment_edit_history_list) {

    private val binding by viewBinding(FragmentEditHistoryListBinding::bind)
    private val viewModel by viewModel<EditHistoryViewModel>(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editHistorySidebar.content {
            val editItems by viewModel.editItems.collectAsState()
            val selectedEdit by viewModel.selectedEdit.collectAsState()

            EditHistorySidebar(
                editItems = editItems,
                selectedEdit = selectedEdit,
                onSelectEdit = { viewModel.select(it.key) },
                onUndoEdit = { onUndo(it) }
            )
        }

        // on opening, always select the first item
        viewModel.select(viewModel.editItems.value.lastOrNull()?.edit?.key)
    }

    private fun onUndo(edit: Edit) {
        if (edit.isUndoable) {
            viewLifecycleScope.launch {
                val element = viewModel.getEditElement(edit)
                UndoDialog(
                    requireContext(),
                    edit,
                    element,
                    viewModel.featureDictionaryLazy,
                    { viewModel.undo(it.key) }
                ).show()
            }
        } else {
            context?.toast(R.string.toast_undo_unavailable, Toast.LENGTH_LONG)
        }
    }
}
