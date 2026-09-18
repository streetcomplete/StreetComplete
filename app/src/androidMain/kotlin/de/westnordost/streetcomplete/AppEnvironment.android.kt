package de.westnordost.streetcomplete

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import android.view.ContextThemeWrapper
import androidx.compose.foundation.isSystemInDarkTheme
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
import java.util.Locale

@Composable
actual fun AppEnvironment(
    language: String?,
    theme: Theme,
    keepScreenOn: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val systemLocales = Resources.getSystem().configuration.locales
    val darkTheme = when (theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> isSystemInDarkTheme()
    }
    val localizedConfiguration = remember(configuration, systemLocales, language, darkTheme) {
        val locales = (listOfNotNull(language?.let(Locale::forLanguageTag)) +
            (0 until systemLocales.size()).map { systemLocales[it]!! }).distinct()
        val localeList = LocaleList(*locales.toTypedArray())
        // Compose Locale.current and non-composable resource/formatting code read these defaults.
        Locale.setDefault(locales.first())
        LocaleList.setDefault(localeList)
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
        context.activity()?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    DisposableEffect(view) { onDispose { view.keepScreenOn = false } }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        LocalLayoutDirection provides if (localizedConfiguration.layoutDirection == 1) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalAppLocale provides AppLocale(language, androidx.compose.ui.text.intl.LocaleList.current),
        content = content,
    )
}

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}
