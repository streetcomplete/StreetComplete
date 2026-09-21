package de.westnordost.streetcomplete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.screens.about.AboutNavHost
import de.westnordost.streetcomplete.screens.settings.SettingsNavHost
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialScreen
import de.westnordost.streetcomplete.screens.user.UserNavHost
import platform.Foundation.NSUserDefaults

private enum class Screen { About, Settings, User, IntroTutorial, OverlaysTutorial }

/** Allows opening a screen directly for development, e.g.
 *  xcrun simctl launch booted <bundle id> -screen Changelog */
private val initialScreen: Screen?
    get() = NSUserDefaults.standardUserDefaults.stringForKey("screen")
        ?.let { name -> Screen.entries.find { it.name == name } }

/** Temporary launcher to try out the screens that have been migrated to Compose Multiplatform
 *  already, until the real main screen works on iOS */
@Composable
fun IosApp() {
    var screen by remember { mutableStateOf(initialScreen) }

    Surface(Modifier.fillMaxSize()) {
        when (screen) {
            null -> LauncherScreen(onClickScreen = { screen = it })
            Screen.About -> AboutNavHost(
                onClickBack = { screen = null },
            )
            Screen.Settings -> SettingsNavHost(
                onClickBack = { screen = null },
            )
            Screen.User -> UserNavHost(
                launchAuth = false,
                onClickBack = { screen = null },
            )
            Screen.IntroTutorial -> IntroTutorialScreen(
                onDismissRequest = { screen = null },
                onFinished = { screen = null }
            )
            Screen.OverlaysTutorial -> OverlaysTutorialScreen(
                onDismissRequest = { screen = null },
                onFinished = { screen = null }
            )
        }
    }
}

@Composable
private fun LauncherScreen(onClickScreen: (Screen) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "StreetComplete",
            style = MaterialTheme.typography.h4,
        )
        for (entry in Screen.entries) {
            TextButton(onClick = { onClickScreen(entry) }) {
                Text(entry.name)
            }
        }
    }
}
