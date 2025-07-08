package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.getLanguageName
import de.westnordost.streetcomplete.util.ktx.languageName
import de.westnordost.streetcomplete.util.ktx.scriptName

/** A button where one can select a language from the given [languageTags] */
@Composable
fun SelectLanguageButton(
    languageTags: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { showDropdown = true },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_24dp),
                contentDescription = stringResource(R.string.quest_streetName_add_language)
            )
        }
    }

    DropdownMenu(
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false },
    ) {
        for (languageTag in languageTags) {
            DropdownMenuItem(onClick = { onSelect(languageTag) }) {
                Text(getLanguageMenuItemTitle(languageTag))
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun getLanguageMenuItemTitle(languageTag: String): String {
    if (languageTag.isEmpty()) return stringResource(R.string.quest_streetName_menuItem_nolanguage)
    if (languageTag == "international") return stringResource(R.string.quest_streetName_menuItem_international)
    val isRomanization = languageTag.endsWith("Latn")
    val locale = Locale(languageTag)

    val languageName = locale.languageName
    val nativeLanguageName = locale.getLanguageName(locale)
    val scriptName = locale.scriptName ?: locale.script

    val displayLanguage =
        if (languageName == null) {
            languageTag
        }
        else if (languageName == nativeLanguageName || nativeLanguageName == null) {
            stringResource(
                R.string.quest_streetName_menuItem_language_simple,
                languageTag, languageName
            )
        }
        else {
            stringResource(
                R.string.quest_streetName_menuItem_language_native,
                languageTag, nativeLanguageName, languageName
            )
        }
    return if (isRomanization) {
        stringResource(
            R.string.quest_streetName_menuItem_with_script,
            displayLanguage, scriptName
        )
    } else {
        displayLanguage
    }
}
