package de.westnordost.streetcomplete.util.ktx

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat
import androidx.core.net.toUri
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** return the number of density independent pixels for the given pixels */
fun Context.pxToDp(px: Float): Float = px / resources.displayMetrics.density

/** return the number of pixels for the given density independent pixels */
fun Context.dpToPx(dip: Float): Float = dip * resources.displayMetrics.density

/** return the number of pixels for the given scalable pixels */
fun Context.spToPx(sp: Float): Float = sp * resources.displayMetrics.scaledDensity

/** return the number of density independent pixels for the given pixels */
fun Context.pxToDp(px: Int): Float = px / resources.displayMetrics.density

/** return the number of pixels for the given density independent pixels */
fun Context.dpToPx(dp: Int): Float = dp * resources.displayMetrics.density

/** return the number of pixels for the given scalable pixels */
fun Context.spToPx(sp: Int): Float = sp * resources.displayMetrics.scaledDensity

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun View.showKeyboard(): Boolean? =
    context?.inputMethodManager?.showSoftInput(this, SHOW_IMPLICIT)

fun View.hideKeyboard(): Boolean? =
    context?.inputMethodManager?.hideSoftInputFromWindow(windowToken, 0)

val Context.isLocationEnabled: Boolean get() = LocationManagerCompat.isLocationEnabled(locationManager)
val Context.hasLocationPermission: Boolean get() = hasPermission(ACCESS_FINE_LOCATION)

val Context.hasCameraPermission: Boolean get() = hasPermission(CAMERA)

private val Context.inputMethodManager get() = getSystemService<InputMethodManager>()!!
private val Context.locationManager get() = getSystemService<LocationManager>()!!

/** Await a call from a broadcast once and return it */
suspend fun Context.awaitReceiverCall(intentFilter: IntentFilter): Intent =
    suspendCancellableCoroutine { continuation ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                unregisterReceiver(this)
                continuation.resume(intent)
            }
        }
        registerReceiver(receiver, intentFilter)
        continuation.invokeOnCancellation { unregisterReceiver(receiver) }
    }

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

fun Context.openUri(uri: String): Boolean {
    return try {
        startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
