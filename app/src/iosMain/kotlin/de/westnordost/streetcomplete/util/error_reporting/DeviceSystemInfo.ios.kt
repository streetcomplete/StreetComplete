package de.westnordost.streetcomplete.util.error_reporting

import platform.UIKit.UIDevice

actual fun getDeviceSystemInfo(): String =
    with(UIDevice.currentDevice) { "Apple $model, $systemName $systemVersion" }
