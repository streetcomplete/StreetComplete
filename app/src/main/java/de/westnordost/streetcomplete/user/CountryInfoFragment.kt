package de.westnordost.streetcomplete.user

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnStart
import de.westnordost.streetcomplete.R
import kotlinx.android.synthetic.main.fragment_country_info_dialog.*
import java.util.*
import kotlin.math.min
import kotlin.math.pow

/** Shows the details for a certain quest type as a fake-dialog. */
class CountryInfoFragment : AbstractInfoFakeDialogFragment(R.layout.fragment_country_info_dialog) {

    // need to keep the animators here to be able to clear them on cancel
    private var counterAnimation: ValueAnimator? = null
    private var circularRevealAnimator: ObjectAnimator? = null

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onDestroy() {
        super.onDestroy()
        counterAnimation?.cancel()
        counterAnimation = null
        circularRevealAnimator?.cancel()
        circularRevealAnimator = null
    }

    /* ---------------------------------------- Interface --------------------------------------- */

    fun show(countryCode: String, questCount: Int, countryFlagBubbleView: View) {
        show(countryFlagBubbleView)
        circularRevealAnimator?.cancel()
        val revealAnim = createCircularRevealAnimator()
        revealAnim.start()
        circularRevealAnimator = revealAnim

        titleImageView.setImageResource(getFlagResId(countryCode))
        countryNameText.text = Locale("", countryCode).displayCountry
        solvedQuestsText.text = ""
        val scale = (0.4 + min( questCount / 100.0, 1.0)*0.6).toFloat()
        solvedQuestsContainer.visibility = View.INVISIBLE
        solvedQuestsContainer.scaleX = scale
        solvedQuestsContainer.scaleY = scale

        counterAnimation?.cancel()
        val anim = ValueAnimator.ofInt(0, questCount)

        anim.doOnStart { solvedQuestsContainer.visibility = View.VISIBLE }
        anim.duration = (questCount * 150.0).pow(0.75).toLong()
        anim.addUpdateListener { solvedQuestsText?.text = it.animatedValue.toString() }
        anim.interpolator = DecelerateInterpolator()
        anim.startDelay = ANIMATION_TIME_IN_MS
        anim.start()
        counterAnimation = anim
    }

    override fun dismiss() {
        super.dismiss()
        circularRevealAnimator?.cancel()
        val revealAnim = createCircularHideAnimator()
        revealAnim.start()
        circularRevealAnimator = revealAnim
    }

    private fun getFlagResId(countryCode: String): Int {
        val lowerCaseCountryCode = countryCode.toLowerCase(Locale.US).replace('-', '_')
        return resources.getIdentifier("ic_flag_$lowerCaseCountryCode", "drawable", requireContext().packageName)
    }

    private fun createCircularRevealAnimator(): ObjectAnimator {
        val anim = ObjectAnimator.ofFloat(titleView, "circularity", 1f, 0f)
        anim.interpolator = AccelerateInterpolator()
        anim.duration = ANIMATION_TIME_IN_MS
        return anim
    }

    private fun createCircularHideAnimator(): ObjectAnimator {
        val anim = ObjectAnimator.ofFloat(titleView, "circularity", 0f, 1f)
        anim.interpolator = DecelerateInterpolator()
        anim.duration = ANIMATION_TIME_OUT_MS
        return anim
    }
}
