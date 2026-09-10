package de.westnordost.streetcomplete.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication

@Composable
internal actual fun PlatformKeepScreenOnEffect(enabled: Boolean) {
    DisposableEffect(enabled) {
        UIApplication.sharedApplication.idleTimerDisabled = enabled
        onDispose {
            if (enabled) UIApplication.sharedApplication.idleTimerDisabled = false
        }
    }
}

@Composable
internal actual fun ApplyApplicationLanguageEffect(language: String?) {
    DisposableEffect(language) {
        val defaults = NSUserDefaults.standardUserDefaults
        if (language == null) defaults.removeObjectForKey("AppleLanguages")
        else defaults.setObject(listOf(language), forKey = "AppleLanguages")
        defaults.synchronize()
        onDispose { }
    }
}
