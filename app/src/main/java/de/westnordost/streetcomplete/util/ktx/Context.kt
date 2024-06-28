package de.westnordost.streetcomplete.util.ktx

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat
import androidx.core.net.toUri
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

val Context.isLocationEnabled: Boolean get() = LocationManagerCompat.isLocationEnabled(locationManager)
val Context.hasLocationPermission: Boolean get() = hasPermission(ACCESS_FINE_LOCATION)

private val Context.locationManager get() = getSystemService<LocationManager>()!!

fun Context.sendEmail(email: String, subject: String, text: String? = null) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, ApplicationConstants.USER_AGENT + " " + subject)
        if (text != null) {
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        toast(R.string.no_email_client)
    }
}

fun Context.openUri(uri: String): Boolean =
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
