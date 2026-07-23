package de.westnordost.streetcomplete.screens.settings.debug

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.ExpandableSearchField
import de.westnordost.streetcomplete.ui.common.SearchIcon
import de.westnordost.streetcomplete.ui.common.TopAppBarWithContent

@Composable
fun ShowQuestFormsTopAppBar(
    onClickBack: () -> Unit,
    search: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSearch by rememberSaveable { mutableStateOf(false) }

    fun setShowSearch(value: Boolean) {
        showSearch = value
        if (!value) onSearchChange("")
    }

    TopAppBarWithContent(
        title = { Text("Show Quest Forms") },
        modifier = modifier,
        navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        actions = { IconButton(onClick = { setShowSearch(!showSearch) }) { SearchIcon() } },
    ) {
        ExpandableSearchField(
            expanded = showSearch,
            onDismiss = { setShowSearch(false) },
            search = search,
            onSearchChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface,
                backgroundColor = MaterialTheme.colors.surface
            ),
            keyboardOptions = KeyboardOptions(hintLocales = LocaleList.current),
        )
    }
}
