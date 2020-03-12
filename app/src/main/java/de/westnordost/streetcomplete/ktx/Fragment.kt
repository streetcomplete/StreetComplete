package de.westnordost.streetcomplete.ktx

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

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