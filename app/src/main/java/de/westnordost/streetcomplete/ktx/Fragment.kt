package de.westnordost.streetcomplete.ktx

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.fragment.app.Fragment

fun Fragment.tryStartActivity(intent: Intent): Boolean {
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
