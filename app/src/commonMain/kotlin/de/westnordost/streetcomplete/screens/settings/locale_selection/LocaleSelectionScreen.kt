package de.westnordost.streetcomplete.screens.settings.locale_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.ExpandableSearchField
import de.westnordost.streetcomplete.ui.common.SearchIcon
import de.westnordost.streetcomplete.ui.common.TopAppBarWithContent
import de.westnordost.streetcomplete.util.ktx.getDisplayName
import org.jetbrains.compose.resources.stringResource

@Composable
fun LocaleSelectionScreen(
    viewModel: LocaleSelectionViewModel,
    onClickBack: () -> Unit,
) {
    val selectableLocales by viewModel.selectableLocales.collectAsState()
    val selectedLocale by viewModel.selectedLocale.collectAsState()

    val localeNames by remember { derivedStateOf {
        selectableLocales?.associateWith { it.getDisplayNameOrLanguageTag() }.orEmpty()
    } }

    var searchText by rememberSaveable { mutableStateOf("") }

    // languages are sorted alphabetically by their display name
    val sortedAndFilteredSelectableLocales by remember { derivedStateOf {
        listOf(null) + selectableLocales
            ?.filter { localeNames[it].orEmpty().startsWith(searchText, ignoreCase = true) }
            ?.sortedBy { localeNames[it]?.lowercase() }
            .orEmpty()
    } }

    Column(Modifier.fillMaxSize()) {
        LocaleSelectionTopAppBar(
            onClickBack = onClickBack,
            search = searchText,
            onSearchChange = { searchText = it },
        )
        val insets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ).asPaddingValues()
        LocaleSelectionList(
            locales = sortedAndFilteredSelectableLocales,
            selectedLocale = selectedLocale,
            onSelect = { viewModel.setSelectedLocale(it) },
            modifier = Modifier
                .fillMaxHeight()
                .consumeWindowInsets(insets),
            contentPadding = insets
        )
    }
}

@Composable
private fun LocaleSelectionTopAppBar(
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
        title = { Text(stringResource(Res.string.pref_title_language_select2)) },
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
        )
    }
}

@Composable
private fun LocaleSelectionList(
    locales: List<Locale?>,
    selectedLocale: Locale?,
    onSelect: (locale: Locale?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(locales, key = { it?.toLanguageTag().orEmpty() }) { locale ->
            val isSelected = selectedLocale == locale
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colors.surface)
                    .animateItem()
                    .selectable(isSelected) { onSelect(locale) }
                    .padding(horizontal = 24.dp)
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        locale?.let { it.getDisplayNameOrLanguageTag() }
                            ?: stringResource(Res.string.language_default)
                    )
                }
                RadioButton(selected = isSelected, onClick = null)
            }
        }
    }
}

private fun Locale.getDisplayNameOrLanguageTag(): String =
    getDisplayName(this) ?: toLanguageTag()
