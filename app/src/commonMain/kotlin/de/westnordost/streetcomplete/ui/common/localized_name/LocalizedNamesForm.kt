package de.westnordost.streetcomplete.ui.common.localized_name

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_add_24
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.quest_openingHours_delete
import de.westnordost.streetcomplete.resources.quest_streetName_add_language
import de.westnordost.streetcomplete.resources.quest_streetName_menuItem_language
import de.westnordost.streetcomplete.resources.quest_streetName_menuItem_international
import de.westnordost.streetcomplete.resources.quest_streetName_menuItem_nolanguage
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.util.ktx.displayName
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/** Edit a list of [localizedNames] of at most [languageTags] different languages.*/
@Composable
fun LocalizedNamesForm(
    localizedNames: List<LocalizedName>,
    onChanged: (List<LocalizedName>) -> Unit,
    languageTags: List<String>,
    modifier: Modifier = Modifier,
) {
    val selectableLanguageTags = remember(languageTags, localizedNames) {
        languageTags - localizedNames.map { it.languageTag }
    }
    val selectableLanguageTagsInFirstRow = remember(selectableLanguageTags) {
        listOf("") + selectableLanguageTags
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in localizedNames.indices) {
            val isFirst = i == 0

            // first entry is bold (the first entry is supposed to be the "default language", I
            // hope that comes across to the users like this. Otherwise, a text hint is necessary)
            val fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal

            // in first entry user may select "unspecified language" to cover cases where
            // the default name is no specific language, see
            // https://wiki.openstreetmap.org/wiki/Multilingual_names#Sardegna_.28Sardinia.29
            val selectableLanguageTagsInThisRow =
                if (isFirst) selectableLanguageTagsInFirstRow
                else selectableLanguageTags

            ProvideTextStyle(LocalTextStyle.current.merge(fontWeight = fontWeight)) {
                LocalizedNameRow(
                    localizedName = localizedNames[i],
                    onChange = { localizedName ->
                        val result = localizedNames.toMutableList()
                        result[i] = localizedName
                        onChanged(result)
                    },
                    onDelete = {
                        val result = localizedNames.toMutableList()
                        result.removeAt(i)
                        onChanged(result)
                    },
                    languageTags = selectableLanguageTagsInThisRow,
                    isDeleteVisible = !isFirst,
                )
            }
        }

        if (selectableLanguageTags.isNotEmpty()) {
            DropdownButton(
                items = selectableLanguageTags,
                onSelectedItem = { languageTag ->
                    val result = localizedNames.toMutableList()
                    result.add(LocalizedName(languageTag, ""))
                    onChanged(result)
                },
                modifier = Modifier.width(64.dp),
                style = ButtonStyle.Outlined,
                showDropDownArrow = false,
                itemContent = { Text(getLanguageMenuItemTitle(it)) }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add_24),
                    contentDescription = stringResource(Res.string.quest_streetName_add_language)
                )
            }
        }
    }
}

/** One row in the localized names form: change language, edit name, delete row */
@Composable
private fun LocalizedNameRow(
    localizedName: LocalizedName,
    onChange: (LocalizedName) -> Unit,
    onDelete: () -> Unit,
    languageTags: List<String>,
    isDeleteVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val languageTag = localizedName.languageTag

    var nameState by remember { mutableStateOf(TextFieldValue(localizedName.name)) }
    if (localizedName.name != nameState.text) {
        nameState = nameState.copy(text = localizedName.name)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        DropdownButton(
            items = languageTags,
            selectedItem = languageTag,
            onSelectedItem = { onChange(localizedName.copy(languageTag = it)) },
            modifier = Modifier.width(64.dp),
            style = ButtonStyle.Text,
            showDropDownArrow = false,
            itemContent = { Text(getLanguageMenuItemTitle(it)) }
        ) {
            Text(
                text = if (languageTag == "international") "🌍" else languageTag,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
            )
        }

        TextField(
            value = nameState,
            onValueChange = {
                nameState = it
                onChange(localizedName.copy(name = it.text))
            },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(
                hintLocales =
                    if (languageTag != "" && languageTag != "international") LocaleList(languageTag)
                    else null,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
        )

        if (isDeleteVisible) {
            IconButton(
                onClick = onDelete,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete_24),
                    contentDescription = stringResource(Res.string.quest_openingHours_delete)
                )
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Composable
private fun getLanguageMenuItemTitle(languageTag: String): String {
    if (languageTag.isEmpty()) return stringResource(Res.string.quest_streetName_menuItem_nolanguage)
    if (languageTag == "international") return stringResource(Res.string.quest_streetName_menuItem_international)
    val locale = Locale(languageTag)

    val languageName = locale.displayName

    return if (languageName == null) {
        languageTag
    } else {
        stringResource(Res.string.quest_streetName_menuItem_language, languageTag, languageName)
    }
}

@Preview
@Composable
private fun LocalizedNamesFormPreview() {
    var localizedNames: List<LocalizedName> by remember { mutableStateOf(emptyList()) }
    LocalizedNamesForm(
        localizedNames = localizedNames,
        onChanged = { localizedNames = it },
        languageTags = listOf("de", "pt-BR", "sr-Latn", "international")
    )
}
