package de.westnordost.streetcomplete.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import java.util.Locale

@Composable
internal actual fun PlatformKeepScreenOnEffect(enabled: Boolean) {
    DisposableEffect(enabled) {
        val inhibitor = if (enabled) startSleepInhibitor() else null
        onDispose { inhibitor?.destroy() }
    }
}

@Composable
internal actual fun ApplyApplicationLanguageEffect(language: String?) {
    DisposableEffect(language) {
        Locale.setDefault(language?.let(Locale::forLanguageTag) ?: systemLocale)
        onDispose { }
    }
}

private val systemLocale: Locale = Locale.getDefault()

private fun startSleepInhibitor(): Process? = runCatching {
    val os = System.getProperty("os.name").lowercase()
    when {
        os.contains("mac") -> ProcessBuilder("caffeinate", "-dimsu").start()
        os.contains("linux") -> ProcessBuilder(
            "systemd-inhibit",
            "--what=idle:sleep",
            "--why=StreetComplete keep-screen-on setting",
            "--mode=block",
            "sleep",
            "infinity",
        ).start()
        os.contains("windows") -> ProcessBuilder(
            "powershell",
            "-NoProfile",
            "-Command",
            WINDOWS_SLEEP_INHIBITOR,
        ).start()
        else -> null
    }
}.getOrNull()

private const val WINDOWS_SLEEP_INHIBITOR =
    "Add-Type -TypeDefinition 'using System; using System.Runtime.InteropServices; " +
        "public class Power { [DllImport(\"kernel32.dll\")] public static extern uint " +
        "SetThreadExecutionState(uint f); }'; " +
        "[Power]::SetThreadExecutionState(0x80000003); while (\$true) { Start-Sleep 30 }"
