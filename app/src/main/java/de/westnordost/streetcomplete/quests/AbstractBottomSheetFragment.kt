package de.westnordost.streetcomplete.quests

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import androidx.annotation.UiThread
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog

import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams

import de.westnordost.streetcomplete.R


import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import de.westnordost.osmapi.map.data.LatLon

abstract class AbstractBottomSheetFragment : Fragment(), IsCloseableBottomSheet {
    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var closeButton: View
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheet = view.findViewById(R.id.bottomSheet)
        bottomSheet.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            // not immediately because this is called during layout change (view.getTop() == 0)
            mainHandler.post { this.updateCloseButtonVisibility() }
        }

        closeButton = view.findViewById(R.id.closeButton)
        closeButton.setOnClickListener { activity?.onBackPressed() }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        val titleSpeechBubble = view.findViewById<View>(R.id.speechBubbleTitleContainer)
        titleSpeechBubble.setOnClickListener {
            bottomSheetBehavior.apply {
                if (state == STATE_EXPANDED)
                    state = STATE_COLLAPSED
                else if (state == STATE_COLLAPSED)
                    state = STATE_EXPANDED
            }
        }

        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                updateCloseButtonVisibility()
            }
        })

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bottomSheetBehavior.state = STATE_EXPANDED
        }

        if (savedInstanceState == null) {
            view.findViewById<View>(R.id.speechBubbleTitleContainer).startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.inflate_title_bubble)
            )

            view.findViewById<View>(R.id.speechbubbleContentContainer).startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.inflate_answer_bubble)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // I need to do everything myself... (AppCompactActivity only does this after calling this
        // method. Genius!)
        resources.updateConfiguration(newConfig, resources.displayMetrics)

        bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.quest_form_peekHeight)
        view?.findViewById<View>(R.id.bottomSheetContainer)?.let {
            it.setBackgroundResource(R.drawable.speechbubbles_gradient_background)
            it.updateLayoutParams { width = resources.getDimensionPixelSize(R.dimen.quest_form_width) }
        }
    }

    private fun updateCloseButtonVisibility() {
        // this is called asynchronously. It may happen that the activity is already gone when this
        // method is finally called
        val activity = activity?: return
        val toolbarHeight = activity.findViewById<View>(R.id.toolbar).height
        val speechBubbleTopMargin = resources.getDimension(R.dimen.quest_form_speech_bubble_top_margin)
        val coversToolbar = bottomSheet.top < speechBubbleTopMargin + toolbarHeight
        closeButton.visibility = if (coversToolbar) View.VISIBLE else View.INVISIBLE
    }

    @UiThread override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        return false
    }

    /** Request to close the form through user interaction (back button, clicked other quest,..),
     * requires user confirmation if any changes have been made  */
    @UiThread override fun onClickClose(onConfirmed: () -> Unit) {
        if (!isRejectingClose()) {
            onDiscard()
            onConfirmed()
        } else {
            activity?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.confirmation_discard_title)
                    .setPositiveButton(R.string.confirmation_discard_positive) { _, _ ->
                        onDiscard()
                        onConfirmed()
                    }
                    .setNegativeButton(R.string.confirmation_discard_negative, null)
                    .show()
            }
        }
    }

    /** returns whether this form should not be closeable without confirmation */
    open fun isRejectingClose(): Boolean = false

    protected open fun onDiscard() {}
}
