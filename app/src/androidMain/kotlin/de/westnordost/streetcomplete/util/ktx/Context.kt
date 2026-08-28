package de.westnordost.streetcomplete.util.ktx

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import androidx.core.net.toUri

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

val Context.currentDisplay: Display get() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display!!
    } else {
        getSystemService<WindowManager>()!!.defaultDisplay
    }

fun Context.openUri(uri: String): Boolean =
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
