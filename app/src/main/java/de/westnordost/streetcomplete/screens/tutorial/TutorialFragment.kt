package de.westnordost.streetcomplete.screens.tutorial

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
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
        val mapTranslate = ctx.resources.dpToPx(-50)
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
        disappearText(binding.tutorialStepIntro)

        delay(200)

        // flashing GPS button appears
        binding.tutorialGpsButton.state = LocationStateButton.State.SEARCHING
        binding.tutorialGpsButton.animate()
            .alpha(1f)
            .setDuration(200)
            .start()

        delay(400)

        // 2nd text fade in
        appearText(binding.tutorialStepSolvingQuests)

        delay(1400)

        // ...and after a few seconds, stops flashing
        binding.tutorialGpsButton.state = LocationStateButton.State.UPDATING

        delay(800)

        // quest pins fall into place
        listOf(binding.questPin1, binding.questPin2, binding.questPin3).forEach { pin ->
            delay(400)

            pin.translationY = ctx.resources.pxToDp(-200)
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
        updateIndicatorDots()
        binding.nextButton.setText(R.string.letsgo)

        // 2nd text fade out
        disappearText(binding.tutorialStepSolvingQuests)

        delay(400)

        // 3rd text fade in
        appearText(binding.tutorialStepStaySafe)

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

    private fun appearText(view: View) {
        view.translationY = view.resources.pxToDp(-100)
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
            .translationY(view.resources.pxToDp(100))
            .withEndAction { view.visibility = View.GONE }
            .start()
    }

    private fun updateIndicatorDots() {
        listOf(binding.dot1, binding.dot2, binding.dot3).forEachIndexed { index, dot ->
            dot.setImageResource(
                if (currentPage == index) {
                    R.drawable.indicator_dot_selected
                } else {
                    R.drawable.indicator_dot_default
                }
            )
        }
    }

    companion object {
        private const val MAX_PAGE_INDEX = 2
    }
}
