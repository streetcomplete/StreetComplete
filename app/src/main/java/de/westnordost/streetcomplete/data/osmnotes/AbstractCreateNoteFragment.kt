package de.westnordost.streetcomplete.data.osmnotes

import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractBottomSheetFragment
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher
import kotlinx.android.synthetic.main.form_leave_note.*
import kotlinx.android.synthetic.main.quest_buttonpanel_done_cancel.*

abstract class AbstractCreateNoteFragment : AbstractBottomSheetFragment() {

    private val attachPhotoFragment: AttachPhotoFragment?
        get() = childFragmentManager.findFragmentById(R.id.attachPhotoFragment) as AttachPhotoFragment

    private val noteText get() = noteInput?.text?.toString().orEmpty().trim()

    protected abstract val layoutResId: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutResId, container, false)

        val bottomSheet = view.findViewById<LinearLayout>(R.id.bottomSheet)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }

        val content = view.findViewById<ViewGroup>(R.id.content)
        content.removeAllViews()
        inflater.inflate(R.layout.form_leave_note, content)

        val buttonPanel = view.findViewById<ViewGroup>(R.id.buttonPanel)
        buttonPanel.removeAllViews()
        inflater.inflate(R.layout.quest_buttonpanel_done_cancel, buttonPanel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.attachPhotoFragment, AttachPhotoFragment())
                .commit()
        }

        noteInput.addTextChangedListener(TextChangedWatcher { updateDoneButtonEnablement() })

        cancelButton.setOnClickListener { activity!!.onBackPressed() }
        doneButton.setOnClickListener { onClickOk() }

        updateDoneButtonEnablement()
    }

    private fun onClickOk() {
        onComposedNote(noteText, attachPhotoFragment?.imagePaths)
    }

    override fun onDiscard() {
        attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose() =
        noteText.isNotEmpty() || attachPhotoFragment?.imagePaths?.isNotEmpty() == true

    private fun updateDoneButtonEnablement() {
        doneButton.isEnabled = !noteText.isEmpty()
    }

    protected abstract fun onComposedNote(text: String, imagePaths: List<String>?)
}
