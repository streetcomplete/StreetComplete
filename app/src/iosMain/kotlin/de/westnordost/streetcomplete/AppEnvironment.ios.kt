package de.westnordost.streetcomplete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.ui.AppLocale
import de.westnordost.streetcomplete.ui.LocalAppLocale
import de.westnordost.streetcomplete.util.locale.appFormattingLocale
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleLanguageDirectionRightToLeft
import platform.Foundation.NSUserDefaults
import platform.Foundation.characterDirectionForLanguage
import platform.Foundation.preferredLanguages
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle

@Composable
actual fun AppEnvironment(
    language: String?,
    theme: Theme,
    keepScreenOn: Boolean,
    content: @Composable () -> Unit,
) {
    var localeRevision by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { localeRevision++ }
    val locale = remember(language, localeRevision) {
        val defaults = NSUserDefaults.standardUserDefaults
        // Read the system list without our previous override, including after returning from Settings.
        defaults.removeObjectForKey("AppleLanguages")
        val systemLanguages = NSLocale.preferredLanguages.filterIsInstance<String>()
        if (language != null) defaults.setObject((listOf(language) + systemLanguages).distinct(), "AppleLanguages")
        appFormattingLocale = language?.let(::Locale)
        Locale(language ?: systemLanguages.firstOrNull() ?: "en")
    }
    val controller = LocalUIViewController.current
    SideEffect {
        controller.overrideUserInterfaceStyle = when (theme) {
            Theme.LIGHT -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
            Theme.DARK -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
            Theme.SYSTEM -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
        }
        UIApplication.sharedApplication.idleTimerDisabled = keepScreenOn
    }
    DisposableEffect(Unit) {
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
    val direction = if (NSLocale.characterDirectionForLanguage(locale.language) == NSLocaleLanguageDirectionRightToLeft) {
        LayoutDirection.Rtl
    } else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalAppLocale provides AppLocale(language, LocaleList.current),
        LocalLayoutDirection provides direction,
        content = content,
    )
}
