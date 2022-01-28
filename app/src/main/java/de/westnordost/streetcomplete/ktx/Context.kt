package de.westnordost.streetcomplete.ktx

import android.Manifest.permission.ACCESS_FINE_LOCATION
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


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
