package de.westnordost.streetcomplete.ui.common

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R

@Composable
fun BackIcon() {
    Icon(painterResource(R.drawable.ic_arrow_back_24dp), stringResource(R.string.action_back))
}

@Composable
fun ClearIcon() {
    Icon(painterResource(R.drawable.ic_clear_24dp), stringResource(R.string.action_clear))
}

@Composable
fun MoreIcon() {
    Icon(painterResource(R.drawable.ic_more_24dp), stringResource(R.string.action_more))
}

@Composable
fun SearchIcon() {
    Icon(painterResource(R.drawable.ic_search_24dp), stringResource(R.string.action_search))
}

@Composable
fun CopyIcon() {
    Icon(painterResource(R.drawable.ic_content_copy_24dp), stringResource(android.R.string.copy))
}

@Composable
fun OpenInBrowserIcon() {
    Icon(painterResource(R.drawable.ic_open_in_browser_24dp), stringResource(R.string.action_open_in_browser))
}

@Composable
fun NextScreenIcon() {
    Icon(painterResource(R.drawable.ic_chevron_next_24dp), null)
}
