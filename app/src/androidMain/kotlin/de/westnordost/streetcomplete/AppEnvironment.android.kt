package de.westnordost.streetcomplete

import android.content.res.Configuration
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.ui.AppLocale
import de.westnordost.streetcomplete.ui.LocalAppLocale
import de.westnordost.streetcomplete.util.ktx.findActivity

@Composable
actual fun AppEnvironment(
    language: String?,
    theme: Theme,
    keepScreenOn: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val darkTheme = theme.isDark
    // AppLocaleUpdater has already made these the process defaults
    val localeList = remember(configuration, language) { appLocales(language) }
    val localizedConfiguration = remember(configuration, localeList, darkTheme) {
        Configuration(configuration).apply {
            setLocales(localeList)
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (darkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
    }
    val localizedContext = remember(context, localizedConfiguration) {
        ContextThemeWrapper(context, 0).apply { applyOverrideConfiguration(localizedConfiguration) }
    }
    val view = LocalView.current
    SideEffect {
        view.keepScreenOn = keepScreenOn
        context.findActivity()?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    DisposableEffect(view) { onDispose { view.keepScreenOn = false } }
    val layoutDirection =
        if (localizedConfiguration.layoutDirection == View.LAYOUT_DIRECTION_RTL) LayoutDirection.Rtl
        else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        LocalLayoutDirection provides layoutDirection,
        LocalAppLocale provides AppLocale(language, androidx.compose.ui.text.intl.LocaleList.current),
        content = content,
    )
}
