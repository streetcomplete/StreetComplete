package de.westnordost.streetcomplete.screens

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon

/** A fragment that shows the back button in its toolbar only when it is displayed as a single
 * detail pane. */
abstract class TwoPaneDetailFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId),
    TwoPaneHeaderFragment.PaneListener {

    private var toolbar: Toolbar? = null
    private var backIcon: Drawable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById<Toolbar>(R.id.toolbar).apply {
            setUpToolbarTitleAndIcon(this)
            backIcon = navigationIcon
        }
    }

    override fun onPanesChanged(singlePane: Boolean) {
        toolbar?.navigationIcon = if (singlePane) backIcon else null
    }
}
