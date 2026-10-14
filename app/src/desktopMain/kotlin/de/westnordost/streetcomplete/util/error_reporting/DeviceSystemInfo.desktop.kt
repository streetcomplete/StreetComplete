package de.westnordost.streetcomplete.util.error_reporting

actual fun getDeviceSystemInfo(): String =
    "${System.getProperty("os.name")} ${System.getProperty("os.version")} (${System.getProperty("os.arch")}), Java ${System.getProperty("java.version")}"
