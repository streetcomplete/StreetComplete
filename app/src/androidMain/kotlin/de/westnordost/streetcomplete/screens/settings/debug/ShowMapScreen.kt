package de.westnordost.streetcomplete.screens.settings.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.screens.main.map2.Map
import de.westnordost.streetcomplete.ui.common.BackIcon

@Composable
fun ShowMapScreen(
    onClickBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Show map") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Map(Modifier.fillMaxSize())
    }
}
