package de.westnordost.streetcomplete.quests

import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.add
import androidx.fragment.app.commit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestButtonpanelDoneCancelBinding
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher

/** Abstract base class for a bottom sheet that lets the user create a note */
abstract class AbstractCreateNoteFragment : AbstractBottomSheetFragment() {

    protected abstract val layoutResId: Int

    protected abstract val noteInput: EditText
    protected abstract val buttonPanel: ViewGroup

    private val buttonsBinding by viewBinding(QuestButtonpanelDoneCancelBinding::bind)

    private val attachPhotoFragment: AttachPhotoFragment?
        get() = childFragmentManager.findFragmentById(R.id.attachPhotoFragment) as AttachPhotoFragment?

    private val noteText get() = noteInput.text?.toString().orEmpty().trim()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutResId, container, false)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutInflater.inflate(R.layout.quest_buttonpanel_done_cancel, buttonPanel)

        if (savedInstanceState == null) {
            childFragmentManager.commit { add<AttachPhotoFragment>(R.id.attachPhotoFragment) }
        }

        noteInput.addTextChangedListener(TextChangedWatcher { updateDoneButtonEnablement() })
        buttonsBinding.cancelButton.setOnClickListener { activity?.onBackPressed() }
        buttonsBinding.doneButton.setOnClickListener { onClickOk() }

        updateDoneButtonEnablement()
    }

    private fun onClickOk() {
        onComposedNote(noteText, attachPhotoFragment?.imagePaths.orEmpty())
    }

    override fun onDiscard() {
        attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose() =
        noteText.isNotEmpty() || attachPhotoFragment?.imagePaths?.isNotEmpty() == true

    private fun updateDoneButtonEnablement() {
        buttonsBinding.doneButton.isEnabled = noteText.isNotEmpty()
    }

    protected abstract fun onComposedNote(text: String, imagePaths: List<String>)
}
