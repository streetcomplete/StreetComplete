package de.westnordost.streetcomplete.util.ktx

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.screens.HasTitle

fun Fragment.openUri(uri: String) = context?.openUri(uri) ?: false

val Fragment.childFragmentManagerOrNull: FragmentManager? get() =
    if (host != null) childFragmentManager else null

val Fragment.viewLifecycleScope get() = viewLifecycleOwner.lifecycleScope

fun Fragment.setUpToolbarTitleAndIcon(toolbar: Toolbar) {
    if (this is HasTitle) {
        toolbar.title = title
        toolbar.subtitle = subtitle
    }

    val typedArray =
        toolbar.context.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.homeAsUpIndicator))
    val attributeResourceId = typedArray.getResourceId(0, 0)
    val backIcon = toolbar.context.getDrawable(attributeResourceId)
    typedArray.recycle()

    toolbar.setNavigationOnClickListener {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    toolbar.navigationIcon = backIcon
}
