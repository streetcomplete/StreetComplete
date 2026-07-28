package de.westnordost.streetcomplete.util.error_reporting

import android.os.Build

actual fun getDeviceSystemInfo(): String =
    "${Build.BRAND}  ${Build.DEVICE}, Android ${Build.VERSION.RELEASE}"
