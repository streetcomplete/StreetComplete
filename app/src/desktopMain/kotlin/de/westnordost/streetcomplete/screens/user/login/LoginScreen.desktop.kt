package de.westnordost.streetcomplete.screens.user.login

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.user_login
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun LoginScreen(viewModel: LoginViewModel, onClickBack: () -> Unit) {
    Column {
        TopAppBar(
            title = { Text(stringResource(Res.string.user_login)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        CenteredLargeTitleHint("Login is unavailable in the desktop development build.")
    }
}
