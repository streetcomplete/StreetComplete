package de.westnordost.streetcomplete.screens.main.bottom_sheet

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.add
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.note_discussion.AttachPhotoFragment
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import org.koin.android.ext.android.inject

/** Abstract base class for a bottom sheet that lets the user create a note */
abstract class AbstractCreateNoteFragment : AbstractBottomSheetFragment() {

    protected abstract val noteInput: EditText
    protected abstract val okButton: View
    protected abstract val gpxButton: View

    private val attachPhotoFragment: AttachPhotoFragment?
        get() = childFragmentManager.findFragmentById(R.id.attachPhotoFragment) as AttachPhotoFragment?

    private val noteText get() = noteInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            childFragmentManager.commit { add<AttachPhotoFragment>(R.id.attachPhotoFragment) }
        }

        noteInput.doAfterTextChanged { updateOkButtonEnablement() }
        okButton.setOnClickListener { onClickOk(false) }
        gpxButton.setOnClickListener { onClickOk(true) }

        updateOkButtonEnablement()
    }

    private fun onClickOk(isGpxNote: Boolean) {
        onComposedNote(noteText, attachPhotoFragment?.imagePaths.orEmpty(), isGpxNote)
    }

    override fun onDiscard() {
        attachPhotoFragment?.deleteImages()
    }

    override fun isRejectingClose() =
        noteText.isNotEmpty() || attachPhotoFragment?.imagePaths?.isNotEmpty() == true

    private fun updateOkButtonEnablement() {
        if (noteText.isNotEmpty()) {
            okButton.popIn()
            if (prefs.getBoolean(Prefs.GPX_BUTTON, false))
                gpxButton.popIn()
        } else {
            okButton.popOut()
            if (prefs.getBoolean(Prefs.GPX_BUTTON, false))
                gpxButton.popOut()
        }
    }

    protected abstract fun onComposedNote(text: String, imagePaths: List<String>, isGpxNote: Boolean)
}
