package de.westnordost.streetcomplete.quests

import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import androidx.core.view.isGone

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FormLeaveNoteBinding
import de.westnordost.streetcomplete.databinding.FragmentCreateNoteBinding
import de.westnordost.streetcomplete.ktx.getLocationInWindow
import de.westnordost.streetcomplete.ktx.hideKeyboard
import de.westnordost.streetcomplete.ktx.viewBinding

/** Bottom sheet fragment with which the user can create a new note, including moving the note */
class CreateNoteFragment : AbstractCreateNoteFragment() {

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding: FragmentCreateNoteBinding get() = _binding!!

    private val bottomSheetBinding get() = binding.questAnswerLayout

    override val bottomSheetContainer get() = bottomSheetBinding.bottomSheetContainer
    override val bottomSheet get() = bottomSheetBinding.bottomSheet
    override val scrollViewChild get() = bottomSheetBinding.scrollViewChild
    override val bottomSheetTitle get() = bottomSheetBinding.speechBubbleTitleContainer
    override val bottomSheetContent get() = bottomSheetBinding.speechbubbleContentContainer
    override val floatingBottomView get() = bottomSheetBinding.okButton
    override val backButton get() = bottomSheetBinding.closeButton
    override val okButton get() = bottomSheetBinding.okButton

    private val contentBinding by viewBinding(FormLeaveNoteBinding::bind, R.id.content)

    override val noteInput get() = contentBinding.noteInput

    interface Listener {
        /** Called when the user wants to leave a note which is not related to a quest  */
        fun onCreatedNote(note: String, imagePaths: List<String>, screenPosition: Point)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCreateNoteBinding.inflate(inflater, container, false)
        inflater.inflate(R.layout.form_leave_note, bottomSheetBinding.content)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.buttonPanel.isGone = true

        if (savedInstanceState == null) {
            binding.markerCreateLayout.markerLayoutContainer.startAnimation(createFallDownAnimation())
        }

        bottomSheetBinding.titleLabel.text = getString(R.string.map_btn_create_note)
        contentBinding.descriptionLabel.text = getString(R.string.create_new_note_description)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.markerCreateLayout.centeredMarkerLayout.setPadding(
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_topOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        )
    }

    private fun createFallDownAnimation(): Animation {
        val a = AnimationSet(false)
        a.startOffset = 200

        val ta = TranslateAnimation(0, 0f, 0, 0f, 1, -0.2f, 0, 0f)
        ta.interpolator = BounceInterpolator()
        ta.duration = 400
        a.addAnimation(ta)

        val aa = AlphaAnimation(0f, 1f)
        aa.interpolator = AccelerateInterpolator()
        aa.duration = 200
        a.addAnimation(aa)

        return a
    }

    override fun onDiscard() {
        super.onDiscard()
        binding.markerCreateLayout.markerLayoutContainer.visibility = View.INVISIBLE
    }

    override fun onComposedNote(text: String, imagePaths: List<String>) {
        /* pressing once on "OK" should first only close the keyboard, so that the user can review
           the position of the note he placed */
        if (contentBinding.noteInput.hideKeyboard() == true) return

        val screenPos = binding.markerCreateLayout.createNoteMarker.getLocationInWindow()
        screenPos.offset(
            binding.markerCreateLayout.createNoteMarker.width / 2,
            binding.markerCreateLayout.createNoteMarker.height / 2
        )

        binding.markerCreateLayout.markerLayoutContainer.visibility = View.INVISIBLE

        listener?.onCreatedNote(text, imagePaths, screenPos)
    }
}
