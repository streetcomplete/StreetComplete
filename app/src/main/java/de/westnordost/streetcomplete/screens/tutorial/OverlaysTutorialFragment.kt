package de.westnordost.streetcomplete.screens.tutorial

import android.animation.TimeAnimator
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentOverlaysTutorialBinding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OverlaysTutorialFragment : Fragment(R.layout.fragment_overlays_tutorial) {

    private var currentPage: Int = 0

    private var shineAnimation: TimeAnimator? = null

    private val binding by viewBinding(FragmentOverlaysTutorialBinding::bind)

    interface Listener {
        fun onOverlaysTutorialFinished()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.respectSystemInsets()
        updateIndicatorDots()
        enableNextButton()

        val anim = TimeAnimator()
        anim.setTimeListener { _, _, deltaTime ->
            binding.shineView1.rotation += deltaTime / 25f
            binding.shineView2.rotation -= deltaTime / 50f
        }
        anim.start()
        shineAnimation = anim
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shineAnimation?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun nextStep() {
        disableNextButton()
        when (currentPage) {
            0 -> {
                currentPage = 1
                step1Transition()
            }
            1 -> {
                currentPage = 2
                step2Transition()
            }
            MAX_PAGE_INDEX -> {
                listener?.onOverlaysTutorialFinished()
            }
        }
    }

    private fun disableNextButton() {
        binding.nextButton.setOnClickListener(null)
        binding.nextButton.isClickable = false
    }

    private fun enableNextButton() {
        binding.nextButton.isClickable = true
        binding.nextButton.setOnClickListener { nextStep() }
    }

    private fun step1Transition() = viewLifecycleScope.launch {
        val ctx = requireContext()

        updateIndicatorDots()

        disappearText(binding.tutorialStepIntro)

        // "explode" overlay button
        binding.overlaysButton.animate()
            .setInterpolator(AnticipateInterpolator())
            .setDuration(450)
            .alpha(0f)
            .scaleX(3f)
            .scaleY(3f)
            .start()

        listOf(binding.shineView1, binding.shineView2).forEach {
            it.animate()
                .setDuration(250)
                .alpha(0f)
                .start()
        }

        // reveal paint roller "behind" the button
        binding.paintRollerView.isInvisible = false
        binding.paintRollerView.rotation = -15f
        binding.paintRollerView.scaleX = 0.1f
        binding.paintRollerView.scaleY = 0.1f
        binding.paintRollerView.translationX = ctx.dpToPx(132)
        binding.paintRollerView.translationY = ctx.dpToPx(66)

        delay(300)

        // move paint roller to start position
        binding.paintRollerView.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(700)
            .translationX(-binding.paintRollerView.width / 4f)
            .translationY(-binding.paintRollerView.height / 4f)
            .rotation(-45f)
            .scaleX(1f)
            .scaleY(1f)
            .start()

        delay(700)

        // move the paint roller from left to right
        (binding.paintRollerView.drawable as? AnimatedVectorDrawable)?.start()

        binding.paintRollerView.animate()
            .setInterpolator(LinearInterpolator())
            .setDuration(1000)
            .translationX(binding.paintRollerView.width.toFloat())
            .translationY(binding.paintRollerView.height.toFloat())
            .start()

        // reveal overlay colors
        binding.overlayImageView.isInvisible = false
        (binding.overlayImageView.drawable as? AnimatedVectorDrawable)?.start()

        delay(200)
        binding.overlayIcon1.isInvisible = false

        delay(400)
        binding.overlayIcon2.isInvisible = false

        appearText(binding.tutorialStepDisplay)

        enableNextButton()
    }

    private fun step2Transition() = viewLifecycleScope.launch {
        val ctx = requireContext()

        updateIndicatorDots()

        binding.nextButton.setText(R.string.letsgo)

        disappearText(binding.tutorialStepDisplay)

        delay(400)

        binding.mapImageContainer.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(900)
            .scaleX(2f)
            .scaleY(2f)
            .rotation(-15f)
            .translationX(ctx.dpToPx(+60))
            .translationY(ctx.dpToPx(-120))
            .start()

        binding.overlaySelectedImageView.isInvisible = false
        (binding.overlaySelectedImageView.drawable as? AnimatedVectorDrawable)?.start()

        delay(600)

        appearText(binding.tutorialStepEdit)

        delay(3000)

        binding.overlaySelectedImageView.setImageResource(R.drawable.overlay_osm_map_edit_done_animated)
        (binding.overlaySelectedImageView.drawable as? AnimatedVectorDrawable)?.start()

        enableNextButton()
    }

    private fun appearText(view: View) {
        view.translationY = view.context.pxToDp(-100)
        view.animate()
            .withStartAction { view.visibility = View.VISIBLE }
            .setDuration(300)
            .alpha(1f)
            .translationY(0f)
            .start()
    }

    private fun disappearText(view: View) {
        view.animate()
            .setDuration(300)
            .alpha(0f)
            .translationY(view.context.pxToDp(100))
            .withEndAction { view.visibility = View.GONE }
            .start()
    }

    private fun updateIndicatorDots() {
        listOf(binding.dot1, binding.dot2, binding.dot3).forEachIndexed { index, dot ->
            dot.setImageResource(
                if (currentPage == index) R.drawable.indicator_dot_selected
                else R.drawable.indicator_dot_default
            )
        }
    }

    companion object {
        private const val MAX_PAGE_INDEX = 2
    }
}
