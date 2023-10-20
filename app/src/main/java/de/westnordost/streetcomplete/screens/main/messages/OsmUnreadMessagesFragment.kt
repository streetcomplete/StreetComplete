package de.westnordost.streetcomplete.screens.main.messages

import android.app.Dialog
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.DecelerateInterpolator
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentUnreadOsmMessageBinding
import de.westnordost.streetcomplete.util.SoundFx
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Fragment that shows a message that the user has X unread messages in his OSM inbox */
class OsmUnreadMessagesFragment : DialogFragment(R.layout.fragment_unread_osm_message) {

    private val soundFx: SoundFx by inject()

    private val binding by viewBinding(FragmentUnreadOsmMessageBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_CustomDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // hide first, to avoid flashing
        binding.mailContainer.alpha = 0.0f
        binding.dialogContainer.setOnClickListener { dismiss() }
        binding.readMailButton.setOnClickListener {
            openUri("https://www.openstreetmap.org/messages/inbox")
            dismiss()
        }
        val unreadMessagesCount = arguments?.getInt(ARG_UNREAD_MESSAGE_COUNT, 0) ?: 0
        binding.unreadMessagesTextView.text = getString(R.string.unread_messages_message, unreadMessagesCount)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { startAnimation() }
        // we want to show a highly custom dialog here with no frame. Without this, the dialog's
        // content is restricted to wrap content, but we want to use whole screen here (for animation)
        dialog.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
        return dialog
    }

    private fun startAnimation() {
        val ctx = requireContext()

        viewLifecycleScope.launch { soundFx.play(R.raw.sliding_envelope) }

        val speechbubbleContentContainer = binding.speechbubbleContentContainer
        val mailOpenImageView = binding.mailOpenImageView
        val mailFrontImageView = binding.mailFrontImageView

        mailFrontImageView.alpha = 0f

        speechbubbleContentContainer.alpha = 0.0f
        speechbubbleContentContainer.visibility = View.VISIBLE
        speechbubbleContentContainer.scaleX = 0.8f
        speechbubbleContentContainer.scaleY = 0.8f
        speechbubbleContentContainer.translationY = ctx.dpToPx(140)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (mailOpenImageView.drawable as? AnimatedVectorDrawable)?.reset()
        }

        binding.mailContainer.rotation = -40f
        binding.mailContainer.rotationY = -45f
        binding.mailContainer.alpha = 0.2f
        binding.mailContainer.translationX = ctx.dpToPx(-400)
        binding.mailContainer.translationY = ctx.dpToPx(60)
        binding.mailContainer.animate().run {
            duration = 400
            startDelay = 200
            interpolator = DecelerateInterpolator()
            rotation(0f)
            rotationY(0f)
            alpha(1f)
            translationX(0f)
            translationY(0f)
            withEndAction {
                (mailOpenImageView.drawable as? AnimatedVectorDrawable)?.start()

                mailFrontImageView.animate().run {
                    duration = 100
                    startDelay = 100
                    alpha(1f)
                    start()
                }

                speechbubbleContentContainer.animate().run {
                    withStartAction { speechbubbleContentContainer.alpha = 0.4f }
                    startDelay = 200
                    duration = 300
                    scaleX(1f)
                    scaleY(1f)
                    alpha(1f)
                    translationY(0f)
                    start()
                }
            }
            start()
        }
    }

    companion object {
        private const val ARG_UNREAD_MESSAGE_COUNT = "unread_message_count"

        fun create(unreadMessagesCount: Int): OsmUnreadMessagesFragment {
            val args = bundleOf(ARG_UNREAD_MESSAGE_COUNT to unreadMessagesCount)
            val f = OsmUnreadMessagesFragment()
            f.arguments = args
            return f
        }
    }
}
