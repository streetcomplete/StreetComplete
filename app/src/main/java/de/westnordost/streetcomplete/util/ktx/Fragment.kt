package de.westnordost.streetcomplete.util.ktx

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.HasTitle

fun Fragment.tryStartActivity(intent: Intent): Boolean {
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

val Fragment.childFragmentManagerOrNull: FragmentManager? get() =
    if (host != null) childFragmentManager else null

val Fragment.viewLifecycleScope get() = viewLifecycleOwner.lifecycleScope

fun Fragment.setUpToolbarTitleAndIcon(toolbar: Toolbar) {
    if (this is HasTitle) {
        toolbar.title = title
        toolbar.subtitle = subtitle
    }

    val typedArray =
        toolbar.context.obtainStyledAttributes(intArrayOf(R.attr.homeAsUpIndicator))
    val attributeResourceId = typedArray.getResourceId(0, 0)
    val backIcon = toolbar.context.getDrawable(attributeResourceId)
    typedArray.recycle()

    toolbar.setNavigationOnClickListener {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    toolbar.navigationIcon = backIcon
}
