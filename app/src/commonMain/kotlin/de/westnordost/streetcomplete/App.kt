package de.westnordost.streetcomplete

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.screens.MainNavHost
import de.westnordost.streetcomplete.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: AppViewModel = koinViewModel()) {
    val theme by viewModel.theme.collectAsState()
    val language by viewModel.language.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    AppEnvironment(language, theme, keepScreenOn) {
        AppTheme(theme.isDark) {
            Surface { MainNavHost(viewModel) }
        }
    }
}

/** Applies native locale/resource settings and window behavior without recreating the app. */
@Composable
expect fun AppEnvironment(
    language: String?,
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
