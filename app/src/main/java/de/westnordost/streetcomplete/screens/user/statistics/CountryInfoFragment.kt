package de.westnordost.streetcomplete.screens.user.statistics

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnStart
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentCountryInfoDialogBinding
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.viewBinding
import java.util.Locale
import kotlin.math.min
import kotlin.math.pow

/** Shows the details for a certain quest type as a fake-dialog. */
class CountryInfoFragment : AbstractInfoFakeDialogFragment(R.layout.fragment_country_info_dialog) {

    private val binding by viewBinding(FragmentCountryInfoDialogBinding::bind)

    override val dialogAndBackgroundContainer get() = binding.dialogAndBackgroundContainer
    override val dialogBackground get() = binding.dialogBackground
    override val dialogContentContainer get() = binding.dialogContentContainer
    override val dialogBubbleBackground get() = binding.dialogBubbleBackground
    override val titleView get() = binding.titleView

    // need to keep the animators here to be able to clear them on cancel
    private var counterAnimation: ValueAnimator? = null
    private var circularRevealAnimator: ObjectAnimator? = null

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onDestroyView() {
        super.onDestroyView()
        counterAnimation?.cancel()
        counterAnimation = null
        circularRevealAnimator?.cancel()
        circularRevealAnimator = null
    }

    /* ---------------------------------------- Interface --------------------------------------- */

    fun show(countryCode: String, count: Int, rank: Int?, countryFlagBubbleView: View) {
        if (!show(countryFlagBubbleView)) return
        circularRevealAnimator?.cancel()
        val revealAnim = createCircularRevealAnimator()
        revealAnim.start()
        circularRevealAnimator = revealAnim

        val flag = requireContext().getDrawable(getFlagResId(countryCode))!!
        binding.titleImageView.setImageDrawable(flag)

        binding.titleView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val flagAspectRatio = flag.intrinsicWidth.toFloat() / flag.intrinsicHeight.toFloat()
                val aspectRatio = view.width.toFloat() / view.height.toFloat()
                val flagWidth: Int
                val flagHeight: Int
                if (flagAspectRatio > aspectRatio) {
                    flagWidth = view.width
                    flagHeight = (view.width / flagAspectRatio).toInt()
                } else {
                    flagWidth = (view.height * flagAspectRatio).toInt()
                    flagHeight = view.height
                }
                val xDiff = view.width - flagWidth
                val yDiff = view.height - flagHeight
                // oval because the shadow is there during the whole animation, rect would look very odd
                // (an oval less so)
                outline.setOval(xDiff / 2, yDiff / 2, flagWidth + xDiff / 2, flagHeight + yDiff / 2)
            }
        }

        val countryLocale = Locale("", countryCode)

        binding.editCountText.text = ""
        val scale = (0.4 + min(count / 100.0, 1.0) * 0.6).toFloat()
        binding.solvedQuestsContainer.visibility = View.INVISIBLE
        binding.solvedQuestsContainer.scaleX = scale
        binding.solvedQuestsContainer.scaleY = scale
        binding.solvedQuestsContainer.setOnClickListener { counterAnimation?.end() }

        val shouldShowRank = rank != null && rank < 500 && count > 50
        binding.countryRankTextView.isGone = !shouldShowRank
        if (shouldShowRank) {
            binding.countryRankTextView.text = resources.getString(
                R.string.user_statistics_country_rank, rank, countryLocale.displayCountry
            )
        }

        binding.wikiLinkButton.text = resources.getString(R.string.user_statistics_country_wiki_link, countryLocale.displayCountry)
        binding.wikiLinkButton.setOnClickListener {
            openUri("https://wiki.openstreetmap.org/wiki/${countryLocale.getDisplayCountry(Locale.UK)}")
        }

        counterAnimation?.cancel()
        val anim = ValueAnimator.ofInt(0, count)

        anim.doOnStart { binding.solvedQuestsContainer.visibility = View.VISIBLE }
        anim.duration = 300 + (count * 500.0).pow(0.6).toLong()
        anim.addUpdateListener { binding.editCountText.text = it.animatedValue.toString() }
        anim.interpolator = DecelerateInterpolator()
        anim.startDelay = ANIMATION_TIME_IN_MS
        anim.start()
        counterAnimation = anim
    }

    override fun dismiss(): Boolean {
        if (!super.dismiss()) return false

        circularRevealAnimator?.cancel()
        val revealAnim = createCircularHideAnimator()
        revealAnim.start()
        circularRevealAnimator = revealAnim
        return true
    }

    private fun getFlagResId(countryCode: String): Int {
        val lowerCaseCountryCode = countryCode.lowercase().replace('-', '_')
        return resources.getIdentifier("ic_flag_$lowerCaseCountryCode", "drawable", requireContext().packageName)
    }

    private fun createCircularRevealAnimator(): ObjectAnimator {
        val anim = ObjectAnimator.ofFloat(binding.titleView, "circularity", 1f, 0f)
        anim.interpolator = AccelerateInterpolator()
        anim.duration = ANIMATION_TIME_IN_MS
        return anim
    }

    private fun createCircularHideAnimator(): ObjectAnimator {
        val anim = ObjectAnimator.ofFloat(binding.titleView, "circularity", 0f, 1f)
        anim.interpolator = DecelerateInterpolator()
        anim.duration = ANIMATION_TIME_OUT_MS
        return anim
    }
}
