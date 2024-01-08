package de.westnordost.streetcomplete.screens

import android.os.Bundle
import android.view.View
import android.view.View.OnLayoutChangeListener
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.preference.PreferenceHeaderFragmentCompat
import de.westnordost.streetcomplete.R

/** A two pane preferences fragment that dispatches updates of its pane state to its children. */
abstract class TwoPaneHeaderFragment : PreferenceHeaderFragmentCompat() {

    private var isSinglePane: Boolean? = null
        set(value) {
            if (value != field && value != null) {
                notifyPaneListeners(value)
            }
            field = value
        }

    // Also pass current pane state to newly attached fragments, but wait until their view was created
    private val fragmentLifecycleCallbacks = object : FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?,
        ) {
            (f as? PaneListener)?.onPanesChanged(isSinglePane ?: true)
        }
    }

    private val onLayoutChangeListener = OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        isSinglePane = slidingPaneLayout.isSlideable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Fragment is not always recreated when size changes, so listen for layout changes
        slidingPaneLayout.addOnLayoutChangeListener(onLayoutChangeListener)
    }

    override fun onStart() {
        super.onStart()
        isSinglePane = slidingPaneLayout.isSlideable
        childFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
    }

    override fun onStop() {
        super.onStop()
        childFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        slidingPaneLayout.removeOnLayoutChangeListener(onLayoutChangeListener)
    }

    private fun notifyPaneListeners(singlePane: Boolean) {
        notifyPaneListener(androidx.preference.R.id.preferences_header, singlePane)
        notifyPaneListener(androidx.preference.R.id.preferences_detail, singlePane)
    }

    private fun notifyPaneListener(@IdRes id: Int, singlePane: Boolean) {
        (childFragmentManager.findFragmentById(id) as? PaneListener)?.onPanesChanged(singlePane)
    }

    interface PaneListener {

        fun onPanesChanged(singlePane: Boolean)
    }
}
