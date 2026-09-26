package de.westnordost.streetcomplete

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.screens.MainNavHost
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    uri: String?,
    onConsumedUri: () -> Unit,
    openSettings: Boolean = false,
    onConsumedSettingsRequest: () -> Unit = {},
    viewModel: AppViewModel = koinViewModel(),
) {
    val theme by viewModel.theme.collectAsState()
    val locale by viewModel.locale.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    AppEnvironment(locale, theme, keepScreenOn) {
        AppTheme(theme.isDark) {
            Surface {
                MainNavHost(uri, onConsumedUri, openSettings, onConsumedSettingsRequest)
            }
        }
    }
}

/** Applies native locale/resource settings and window behavior without recreating the app. */
@Composable
expect fun AppEnvironment(
    locale: Locale?,
    theme: Theme,
    keepScreenOn: Boolean,
    content: @Composable () -> Unit,
)

val Theme.isDark: Boolean
    @Composable get() = when (this) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> isSystemInDarkTheme()
    }
