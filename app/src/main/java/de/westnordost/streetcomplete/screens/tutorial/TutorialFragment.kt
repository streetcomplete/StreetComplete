package de.westnordost.streetcomplete.screens.tutorial

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.pm.ActivityInfo
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentTutorialBinding
import de.westnordost.streetcomplete.screens.main.controls.LocationStateButton
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Shows a short tutorial for first-time users */
class TutorialFragment : Fragment(R.layout.fragment_tutorial) {

    private var currentPage: Int = 0

    private val binding by viewBinding(FragmentTutorialBinding::bind)

    interface Listener {
        fun onTutorialFinished()
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
            2 -> {
                currentPage = 3
                step3Transition()
            }
            MAX_PAGE_INDEX -> {
                listener?.onTutorialFinished()
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

        // magnifier flies towards viewer and fades out
        binding.magnifierImageView.animate()
            .setDuration(500)
            .setInterpolator(AccelerateInterpolator())
            .scaleX(6f).scaleY(6f)
            .alpha(0f)
            .start()

        // map zooms in and tilts
        val mapTranslate = ctx.dpToPx(-50)
        val mapRotate = 50f
        val mapScale = 1.5f

        binding.mapImageView.animate()
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .rotationX(mapRotate)
            .scaleY(mapScale).scaleX(mapScale)
            .translationY(mapTranslate)
            .start()

        binding.mapLightingImageView.animate()
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .rotationX(mapRotate)
            .alpha(0f)
            .scaleY(mapScale).scaleX(mapScale)
            .translationY(mapTranslate)
            .start()

        // 1st text fade out
        val tutorialStepIntro = binding.tutorialStepIntro
        tutorialStepIntro.animate()
            .setDuration(300)
            .alpha(0f)
            .translationY(ctx.pxToDp(100))
            .withEndAction { tutorialStepIntro.visibility = View.GONE }
            .start()

        delay(200)

        // flashing GPS button appears
        binding.tutorialGpsButton.state = LocationStateButton.State.SEARCHING
        binding.tutorialGpsButton.animate()
            .alpha(1f)
            .setDuration(200)
            .start()

        delay(400)

        // 2nd text fade in
        binding.tutorialStepSolvingQuests.translationY = ctx.pxToDp(-100)
        binding.tutorialStepSolvingQuests.animate()
            .withStartAction { binding.tutorialStepSolvingQuests.visibility = View.VISIBLE }
            .setDuration(300)
            .alpha(1f)
            .translationY(0f)
            .start()

        delay(1400)

        // ...and after a few seconds, stops flashing
        binding.tutorialGpsButton.state = LocationStateButton.State.UPDATING

        delay(800)

        // quest pins fall into place
        listOf(binding.questPin1, binding.questPin2, binding.questPin3).forEach { pin ->

            delay(400)

            pin.translationY = ctx.pxToDp(-200)
            pin.animate()
                .setInterpolator(BounceInterpolator())
                .setDuration(400)
                .translationY(0f)
                .alpha(1f)
                .start()
        }

        enableNextButton()
    }

    private fun step2Transition() = viewLifecycleScope.launch {
        val ctx = requireContext()

        updateIndicatorDots()

        // 2nd text fade out
        val tutorialStepSolvingQuests = binding.tutorialStepSolvingQuests
        tutorialStepSolvingQuests.animate()
            .setDuration(300)
            .alpha(0f)
            .translationY(ctx.pxToDp(100))
            .withEndAction { tutorialStepSolvingQuests.visibility = View.GONE }
            .start()

        // 3rd text fade in
        binding.tutorialStepStaySafe.translationY = ctx.pxToDp(-100)

        delay(400)

        binding.tutorialStepStaySafe.animate()
            .withStartAction { binding.tutorialStepStaySafe.visibility = View.VISIBLE }
            .setDuration(300)
            .alpha(1f)
            .translationY(0f)
            .start()

        // quest pins fade out
        listOf(binding.questPin1, binding.questPin2, binding.questPin3).forEach { pin ->
            pin.animate()
                .setInterpolator(AccelerateInterpolator())
                .setDuration(300)
                .alpha(0f)
                .start()
        }

        delay(1400)
        // checkmark fades in and animates

        binding.checkmarkView.animate()
            .setDuration(600)
            .alpha(1f)
            .start()

        (binding.checkmarkView.drawable as? AnimatedVectorDrawable)?.start()

        enableNextButton()
    }

    private fun step3Transition() = viewLifecycleScope.launch {
        val ctx = requireContext()

        updateIndicatorDots()
        binding.nextButton.setText(R.string.letsgo)
        // todo: effectively i just want to show R.string.tutorial_info_fork_message

        val tutorialStepStaySafe = binding.tutorialStepStaySafe
        tutorialStepStaySafe.animate()
            .setDuration(400)
            .alpha(0f)
            .translationY(ctx.pxToDp(100))
            .withEndAction { tutorialStepStaySafe.visibility = View.GONE }
            .start()

        binding.mapImageView.animate()
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .scaleY(0f).scaleX(0f)
            .withEndAction { binding.mapImageView.visibility = View.GONE }
            .start()

        binding.checkmarkView.animate()
            .setDuration(400)
            .alpha(0f)
            .withEndAction { binding.checkmarkView.visibility = View.GONE }
            .start()

        binding.tutorialGpsButton.animate()
            .setDuration(400)
            .alpha(0f)
            .withEndAction { binding.tutorialGpsButton.visibility = View.GONE }
            .start()

        binding.tutorialStepFork.translationY = ctx.pxToDp(-100)

        delay(400)

        binding.tutorialStepFork.animate()
            .withStartAction { binding.tutorialStepFork.visibility = View.VISIBLE }
            .setDuration(300)
            .alpha(1f)
            .translationY(0f)
            .start()
        binding.tutorialStepFork.movementMethod = ScrollingMovementMethod()

        enableNextButton()
    }

    private fun updateIndicatorDots() {
        listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4).forEachIndexed { index, dot ->
            dot.setImageResource(
                if (currentPage == index) R.drawable.indicator_dot_selected
                else R.drawable.indicator_dot_default
            )
        }
    }

    companion object {
        private const val MAX_PAGE_INDEX = 3
    }
}
