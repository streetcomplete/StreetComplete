package de.westnordost.streetcomplete.util.ktx

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope

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
