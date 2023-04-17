package de.westnordost.streetcomplete.screens

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceHeaderFragmentCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon

/** A fragment that implements showing the back button in a toolbar only when it is displayed as a
 * single detail pane in a SlidingPaneLayout. */
abstract class TwoPaneDetailFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setUpToolbarTitleAndIcon(toolbar)

        val activity = requireActivity() as FragmentContainerActivity
        val slidingPane =
            (activity.mainFragment as PreferenceHeaderFragmentCompat).slidingPaneLayout

        // Only display back icon when both panes are shown
        val backIcon = toolbar.navigationIcon
        toolbar.navigationIcon = if (slidingPane.isSlideable) backIcon else null
        // Fragment is not always recreated when size changes, so listen for layout changes here
        slidingPane.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            toolbar.navigationIcon = if (slidingPane.isSlideable) backIcon else null
        }
    }
}
