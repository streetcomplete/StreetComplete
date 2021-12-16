package de.westnordost.streetcomplete.notifications

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.DecelerateInterpolator
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentUnreadOsmMessageBinding
import de.westnordost.streetcomplete.ktx.toPx
import de.westnordost.streetcomplete.ktx.tryStartActivity
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.SoundFx
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Fragment that shows a notification that the user has X unread messages in his OSM inbox */
class OsmUnreadMessagesFragment : DialogFragment(R.layout.fragment_unread_osm_message) {

    @Inject lateinit var soundFx: SoundFx

    private val binding by viewBinding(FragmentUnreadOsmMessageBinding::bind)

    init {
        Injector.applicationComponent.inject(this)
    }

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
            openUrl("https://www.openstreetmap.org/messages/inbox")
            dismiss()
        }
        val unreadMessagesCount = arguments?.getInt(ARG_UNREAD_MESSAGE_COUNT, 0) ?: 0
        binding.unreadMessagesTextView.text = getString(R.string.unread_messages_message, unreadMessagesCount)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setOnShowListener { startAnimation() }
        // we want to show a highly custom dialog here with no frame. Without this, the dialog's
        // content is restricted to wrap content, but we want to use whole screen here (for animation)
        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
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
        speechbubbleContentContainer.translationY = 140f.toPx(ctx)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (mailOpenImageView.drawable as? AnimatedVectorDrawable)?.reset()
        }

        binding.mailContainer.rotation = -40f
        binding.mailContainer.rotationY = -45f
        binding.mailContainer.alpha = 0.2f
        binding.mailContainer.translationX = (-400f).toPx(ctx)
        binding.mailContainer.translationY = (60f).toPx(ctx)
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

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        return tryStartActivity(intent)
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
