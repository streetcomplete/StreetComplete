package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.localized_name.LocalizedNamesForm
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

abstract class AAddLocalizedNameForm<T> : AbstractOsmQuestForm<T>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val selectableLanguages = remember {
            val languages = getSelectableLanguageTags().toMutableList()
            val preferredLanguage = prefs.preferredLanguageForNames
            if (preferredLanguage != null) {
                if (languages.remove(preferredLanguage)) {
                    languages.add(0, preferredLanguage)
                }
            }
            languages
        }

        var localizedNames by rememberSerializable {
            mutableStateOf(listOf(LocalizedName(countryInfo.language.orEmpty(), "")))
        }

        QuestForm(
            answers = Confirm(
                isComplete = localizedNames.isNotEmpty() && localizedNames.all { it.name.isNotBlank() },
                hasChanges = localizedNames.isNotEmpty() && localizedNames.any { it.name.isNotBlank() },
                onClick = {
                    onClickOk(localizedNames)

                    val firstLanguage = localizedNames.firstOrNull()?.languageTag?.takeIf { it.isNotBlank() }
                    if (firstLanguage != null) prefs.preferredLanguageForNames = firstLanguage
                }
            )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showAbbreviationsHint()) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.body2,
                        LocalContentAlpha provides ContentAlpha.medium
                    ) {
                        Text(stringResource(Res.string.quest_streetName_abbreviation_instruction))
                    }
                }
                LocalizedNamesForm(
                    localizedNames = localizedNames,
                    onChanged = { localizedNames = it },
                    languageTags = selectableLanguages,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    protected open fun showAbbreviationsHint(): Boolean = false

    protected open fun getSelectableLanguageTags(): List<String> =
        (countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages).distinct()

    abstract fun onClickOk(names: List<LocalizedName>)
}
