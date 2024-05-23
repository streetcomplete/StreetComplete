package de.westnordost.streetcomplete.screens.main.edithistory

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentEditHistoryListBinding
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows a list of the edit history */
class EditHistoryFragment : Fragment(R.layout.fragment_edit_history_list) {

    private val binding by viewBinding(FragmentEditHistoryListBinding::bind)
    private val viewModel by viewModel<EditHistoryViewModel>(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialPaddingBottom = binding.editHistoryList.paddingBottom
        binding.editHistoryList.respectSystemInsets {
            updatePadding(left = it.left, top = it.top, bottom = it.bottom + initialPaddingBottom)
        }

        val adapter = EditHistoryAdapter(this::onClick)
        binding.editHistoryList.adapter = adapter

        // on opening, always select the first item
        viewModel.select(viewModel.editItems.value.firstOrNull()?.edit?.key)

        observe(viewModel.editItems) { editItems ->
            adapter.edits = editItems
        }
    }

    private fun onClick(editItem: EditItem) {
        if (editItem.isSelected) {
            if (editItem.edit.isUndoable) {
                viewLifecycleScope.launch {
                    val element = viewModel.getEditElement(editItem.edit)
                    UndoDialog(
                        requireContext(),
                        editItem.edit,
                        element,
                        viewModel.featureDictionaryLazy,
                        { viewModel.undo(it.key) }
                    ).show()
                }
            } else {
                context?.toast(R.string.toast_undo_unavailable, Toast.LENGTH_LONG)
            }
        } else {
            viewModel.select(editItem.edit.key)
        }
    }
}
