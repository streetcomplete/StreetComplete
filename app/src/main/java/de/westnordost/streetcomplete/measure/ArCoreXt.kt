package de.westnordost.streetcomplete.measure

import android.content.Context
import com.google.ar.core.*
import com.google.ar.core.TrackingFailureReason.*
import kotlinx.coroutines.delay
import de.westnordost.streetcomplete.R

fun Frame.hitPlane(xPx: Float, yPx: Float): HitResult? =
    hitTest(xPx, yPx)
        .firstOrNull { (it.trackable as? Plane)?.isPoseInPolygon(it.hitPose) == true }

fun Frame.hasFoundPlane(): Boolean =
    getUpdatedTrackables(Plane::class.java).any { it.trackingState == TrackingState.TRACKING }


suspend fun ArCoreApk.getAvailability(context: Context): ArCoreApk.Availability {
    var result = checkAvailability(context)
    // only check again ONCE because of https://github.com/google-ar/arcore-android-sdk/issues/343
    if (result.isTransient) {
        delay(200)
        result = checkAvailability(context)
    }
    return result
}

val TrackingFailureReason.messageResId: Int? get() = when(this) {
    NONE -> null
    BAD_STATE -> R.string.ar_core_tracking_error_bad_state
    INSUFFICIENT_LIGHT -> R.string.ar_core_tracking_error_insufficient_light
    EXCESSIVE_MOTION -> R.string.ar_core_tracking_error_excessive_motion
    INSUFFICIENT_FEATURES -> R.string.ar_core_tracking_error_insufficient_features
    CAMERA_UNAVAILABLE -> R.string.ar_core_tracking_error_camera_unavailable
}
