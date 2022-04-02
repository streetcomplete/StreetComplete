package de.westnordost.streetcomplete.screens.measure

import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingFailureReason.BAD_STATE
import com.google.ar.core.TrackingFailureReason.CAMERA_UNAVAILABLE
import com.google.ar.core.TrackingFailureReason.EXCESSIVE_MOTION
import com.google.ar.core.TrackingFailureReason.INSUFFICIENT_FEATURES
import com.google.ar.core.TrackingFailureReason.INSUFFICIENT_LIGHT
import com.google.ar.core.TrackingFailureReason.NONE
import com.google.ar.core.TrackingState.TRACKING
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import de.westnordost.streetcomplete.R
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan2

fun Frame.hasFoundPlane(): Boolean =
    getUpdatedTrackables(Plane::class.java).any { it.trackingState == TRACKING }

suspend fun ArCoreApk.getAvailability(context: Context): ArCoreApk.Availability {
    var result = checkAvailability(context)
    // only check again ONCE because of https://github.com/google-ar/arcore-android-sdk/issues/343
    if (result.isTransient) {
        delay(200)
        result = checkAvailability(context)
    }
    return result
}

val TrackingFailureReason.messageResId: Int? get() = when (this) {
    NONE -> null
    BAD_STATE -> R.string.ar_core_tracking_error_bad_state
    INSUFFICIENT_LIGHT -> R.string.ar_core_tracking_error_insufficient_light
    EXCESSIVE_MOTION -> R.string.ar_core_tracking_error_excessive_motion
    INSUFFICIENT_FEATURES -> R.string.ar_core_tracking_error_insufficient_features
    CAMERA_UNAVAILABLE -> R.string.ar_core_tracking_error_camera_unavailable
}

val Pose.position: Vector3 get() = Vector3(tx(), ty(), tz())
val Pose.pitch: Float get() {
    val (x, y, z, w) = rotationQuaternion
    return if (0.5 - abs(x * y + z * w) < 0.001) 0f
           else atan2(2 * (x * w - y * z), -x * x + y * y - z * z + w * w)
}

fun Quaternion.difference(other: Quaternion): Quaternion =
    Quaternion.multiply(this, other.inverted())
