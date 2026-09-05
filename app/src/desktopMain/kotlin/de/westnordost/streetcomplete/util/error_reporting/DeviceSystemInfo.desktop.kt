package de.westnordost.streetcomplete.util.error_reporting

actual fun getDeviceSystemInfo(): String = buildString {
    append(System.getProperty("os.name", "unknown"))
    append(' ')
    append(System.getProperty("os.version", "unknown"))
    append(" / ")
    append(System.getProperty("os.arch", "unknown"))
    append(" / Java ")
    append(System.getProperty("java.version", "unknown"))
}
