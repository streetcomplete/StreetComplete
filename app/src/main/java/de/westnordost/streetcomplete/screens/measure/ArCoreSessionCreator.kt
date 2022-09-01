package de.westnordost.streetcomplete.screens.measure

import android.Manifest
import android.app.ActivityManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability.UNKNOWN_CHECKING
import com.google.ar.core.ArCoreApk.Availability.UNKNOWN_TIMED_OUT
import com.google.ar.core.ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import de.westnordost.streetcomplete.screens.measure.ArNotAvailableReason.ANDROID_SDK_VERSION_TOO_OLD
import de.westnordost.streetcomplete.screens.measure.ArNotAvailableReason.AR_CORE_APK_NOT_INSTALLED_OR_TOO_OLD
import de.westnordost.streetcomplete.screens.measure.ArNotAvailableReason.AR_CORE_SDK_TOO_OLD
import de.westnordost.streetcomplete.screens.measure.ArNotAvailableReason.DEVICE_COMPATIBILITY_CHECK_FAILURE
import de.westnordost.streetcomplete.screens.measure.ArNotAvailableReason.DEVICE_COMPATIBILITY_CHECK_TIMED_OUT
import de.westnordost.streetcomplete.screens.measure.ArNotAvailableReason.DEVICE_NOT_COMPATIBLE
import de.westnordost.streetcomplete.screens.measure.ArNotAvailableReason.NO_CAMERA_PERMISSION
import de.westnordost.streetcomplete.util.ActivityForResultLauncher
import de.westnordost.streetcomplete.util.ktx.hasCameraPermission
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/** Creates an ARCore session and ensures that everything is set up to be able to use AR:
 *  - Checks the Android SDK version and OpenGL ES version
 *  - Checks if this device is compatible with AR
 *  - Checks if ARCore has been installed, is up-to-date and if not, requests the user to do this
 *  - Checks for camera permission and if no permission has been granted yet, (re)requests it
 *  - Checks if the ARCore SDK used is still compatible with the current ARCore installation
 */
class ArCoreSessionCreator(
    private val activity: AppCompatActivity,
    private val askUserToAcknowledgeCameraPermissionRationale: suspend () -> Boolean,
    private val features: Set<Session.Feature> = setOf()
) {
    private val requestPermission = ActivityForResultLauncher(activity, ActivityResultContracts.RequestPermission())

    /** Returns an ARCore session (after some back and forth with the user) or a reason why it can't
     *  be created */
    suspend operator fun invoke(): Result {

        /* extra requirements for Sceneform: min Android SDK and OpenGL ES 3.1*/
        if (!hasSufficientAndroidSdkVersion()) {
            return Failure(ANDROID_SDK_VERSION_TOO_OLD)
        }

        if (!hasSufficientOpenGlEsVersion()) {
            return Failure(DEVICE_NOT_COMPATIBLE)
        }

        val availability = getArCoreAvailability()
        if (!availability.isSupported) {
            return when (availability) {
                UNKNOWN_CHECKING, UNKNOWN_TIMED_OUT -> Failure(DEVICE_COMPATIBILITY_CHECK_TIMED_OUT)
                UNSUPPORTED_DEVICE_NOT_CAPABLE ->      Failure(DEVICE_NOT_COMPATIBLE)
                else ->                                Failure(DEVICE_COMPATIBILITY_CHECK_FAILURE)
            }
        }

        val installStatus = requestArCoreInstallation()
        if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
            try {
                awaitArCoreInstallation()
            } catch (e: UnavailableDeviceNotCompatibleException) {
                return Failure(DEVICE_NOT_COMPATIBLE)
            } catch (e: UnavailableUserDeclinedInstallationException) {
                return Failure(AR_CORE_APK_NOT_INSTALLED_OR_TOO_OLD)
            }
        }

        if (!activity.hasCameraPermission) {
            if (!requestCameraPermission()) {
                return Failure(NO_CAMERA_PERMISSION)
            }
        }

        val session: Session
        try {
            session = Session(activity, features)
        } catch (e: UnavailableSdkTooOldException) {
            return Failure(AR_CORE_SDK_TOO_OLD)
        } catch (e: UnavailableDeviceNotCompatibleException) {
            return Failure(DEVICE_NOT_COMPATIBLE)
        }

        return Success(session)
    }

    private fun hasSufficientAndroidSdkVersion(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    private fun hasSufficientOpenGlEsVersion(): Boolean =
        activity.getSystemService<ActivityManager>()!!.deviceConfigurationInfo.glEsVersion.toDouble() >= 3.1

    private suspend fun getArCoreAvailability(): ArCoreApk.Availability =
        ArCoreApk.getInstance().getAvailability(activity)

    private fun requestArCoreInstallation(): ArCoreApk.InstallStatus =
        ArCoreApk.getInstance().requestInstall(activity, true)

    private suspend fun awaitArCoreInstallation() {
        suspendCoroutine {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(lifecycleOwner: LifecycleOwner) {
                    try {
                        /* contrary to what the documentation claims, this method can actually
                           return INSTALL_REQUESTED even if the userRequestedInstall parameter is
                           false. So, we can only continue when it returns INSTALLED */
                        val installStatus = ArCoreApk.getInstance().requestInstall(activity, false)
                        if (installStatus == ArCoreApk.InstallStatus.INSTALLED) {
                            activity.lifecycle.removeObserver(this)
                            it.resume(Unit)
                        }
                    } catch (e: UnavailableException) {
                        activity.lifecycle.removeObserver(this)
                        it.resumeWithException(e)
                    }
                }
            }
            activity.lifecycle.addObserver(observer)
        }
    }

    private suspend fun requestCameraPermission(): Boolean {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            if (!askUserToAcknowledgeCameraPermissionRationale()) {
                return false
            }
        }
        return requestPermission(Manifest.permission.CAMERA)
    }

    sealed class Result
    data class Success(val session: Session) : Result()
    data class Failure(val reason: ArNotAvailableReason) : Result()
}

enum class ArNotAvailableReason {
    ANDROID_SDK_VERSION_TOO_OLD,
    DEVICE_COMPATIBILITY_CHECK_TIMED_OUT,
    DEVICE_COMPATIBILITY_CHECK_FAILURE,
    DEVICE_NOT_COMPATIBLE,
    NO_CAMERA_PERMISSION,
    AR_CORE_APK_NOT_INSTALLED_OR_TOO_OLD,
    AR_CORE_SDK_TOO_OLD
}
