package de.westnordost.streetcomplete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.ui.AppLocale
import de.westnordost.streetcomplete.ui.LocalAppLocale
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleLanguageDirectionRightToLeft
import platform.Foundation.characterDirectionForLanguage
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle

@Composable
actual fun AppEnvironment(
    locale: Locale?,
    theme: Theme,
    keepScreenOn: Boolean,
    content: @Composable () -> Unit,
) {
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
    // AppLocaleUpdater has already put the selected language in front of the preferred languages
    val effectiveLocale = locale ?: Locale.current
    val direction = if (NSLocale.characterDirectionForLanguage(effectiveLocale.language) == NSLocaleLanguageDirectionRightToLeft) {
        LayoutDirection.Rtl
    } else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalAppLocale provides AppLocale(locale, LocaleList.current),
        LocalLayoutDirection provides direction,
        content = content,
    )
}
