package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Fragment that shows (and hides) the undo button, based on whether there is anything to undo */
class UndoButtonFragment : Fragment(R.layout.fragment_undo_button) {

    private val viewModel by viewModel<UndoButtonViewModel>()
    private val undoButton get() = view as ImageButton

    interface Listener {
        fun onClickShowEditHistory()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        undoButton.setOnClickListener { listener?.onClickShowEditHistory() }

        // Don't allow undoing while uploading. Should prevent race conditions.
        // (Undoing quest while also uploading it at the same time)
        observe(viewModel.isUploadInProgress) { undoButton.isEnabled = !it }
        observe(viewModel.hasUndoableEdits) {  view.isGone = !it }
    }
}
