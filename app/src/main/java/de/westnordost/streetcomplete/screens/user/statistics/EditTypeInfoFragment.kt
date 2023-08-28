package de.westnordost.streetcomplete.screens.user.statistics

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnStart
import androidx.core.view.isInvisible
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.databinding.FragmentEditTypeInfoDialogBinding
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.CircularOutlineProvider
import kotlin.math.min
import kotlin.math.pow

/** Shows the details for a certain quest type as a fake-dialog. */
class EditTypeInfoFragment : AbstractInfoFakeDialogFragment(R.layout.fragment_edit_type_info_dialog) {

    private val binding by viewBinding(FragmentEditTypeInfoDialogBinding::bind)

    override val dialogAndBackgroundContainer get() = binding.dialogAndBackgroundContainer
    override val dialogBackground get() = binding.dialogBackground
    override val dialogContentContainer get() = binding.dialogContentContainer
    override val dialogBubbleBackground get() = binding.dialogBubbleBackground
    override val titleView get() = binding.titleView

    // need to keep the animators here to be able to clear them on cancel
    private var counterAnimation: ValueAnimator? = null

    /* ---------------------------------------- Lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleView.outlineProvider = CircularOutlineProvider
    }

    override fun onDestroyView() {
        super.onDestroyView()
        counterAnimation?.cancel()
        counterAnimation = null
    }

    /* ---------------------------------------- Interface --------------------------------------- */

    fun show(editType: EditType, count: Int, questBubbleView: View) {
        if (!show(questBubbleView)) return
        binding.titleView.setImageResource(editType.icon)
        binding.titleText.text = resources.getString(editType.title, *Array(10) { "…" })
        binding.editCountText.text = ""
        val scale = (0.4 + min(count / 100.0, 1.0) * 0.6).toFloat()
        binding.editCountContainer.visibility = View.INVISIBLE
        binding.editCountContainer.scaleX = scale
        binding.editCountContainer.scaleY = scale
        binding.editCountContainer.setOnClickListener { counterAnimation?.end() }
        val wikiLink = editType.wikiLink
        binding.wikiLinkButton.isInvisible = wikiLink == null
        if (wikiLink != null) {
            binding.wikiLinkButton.setOnClickListener {
                openUri("https://wiki.openstreetmap.org/wiki/$wikiLink")
            }
        }

        counterAnimation?.cancel()
        val anim = ValueAnimator.ofInt(0, count)

        anim.doOnStart { binding.editCountContainer.visibility = View.VISIBLE }
        anim.duration = 300 + (count * 500.0).pow(0.6).toLong()
        anim.addUpdateListener { binding.editCountText.text = it.animatedValue.toString() }
        anim.interpolator = DecelerateInterpolator()
        anim.startDelay = ANIMATION_TIME_IN_MS
        anim.start()
        counterAnimation = anim
    }
}
