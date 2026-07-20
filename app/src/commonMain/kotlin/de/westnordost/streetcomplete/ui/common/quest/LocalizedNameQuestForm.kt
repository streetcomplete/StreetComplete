package de.westnordost.streetcomplete.ui.common.quest

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
import de.westnordost.streetcomplete.ApplicationConstants.MAX_OSM_TAG_VALUE_LENGTH
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.localized_name.LocalizedNamesForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Quest form in which the user inputs a set of names in possibly different languages. Selectable
 *  are only (locally) official languages and languages commonly seen on street signs that are not
 *  official, determined by the given [countryInfo].
 *
 *  This quest form always has two other answer options: An info dialog that explains what to do
 *  when the IME doesn't have the characters necessary to type what is on the sign, and an option to
 *  answer that there is no name sign, in which case an empty list is returned.
 *  The form can be (pre) filled by specifying [initialLocalizedNames].
 *  */
@Composable
fun LocalizedNameQuestForm(
    on: (QuestAction<List<LocalizedName>>) -> Unit,
    countryInfo: CountryInfo,
    initialLocalizedNames: List<LocalizedName>?,
    modifier: Modifier = Modifier,
    hint: @Composable (() -> Unit)? = null,
    noNameConfirmationText: @Composable (() -> Unit)? = null,
    otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
    preferences: Preferences = koinInject(),
) {
    val selectableLanguages = remember {
        preferences.getLanguagesWithPreferredFirst(
            countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages
        )
    }

    var localizedNames by rememberSerializable(initialLocalizedNames) {
        mutableStateOf(
            initialLocalizedNames
                ?: listOf(LocalizedName(selectableLanguages.firstOrNull().orEmpty(), ""))
        )
    }

    var showKeyboardInfo by remember { mutableStateOf(false) }
    var confirmNoName by remember { mutableStateOf(false) }

    QuestForm(
        on = on,
        isComplete =
            localizedNames.isNotEmpty() &&
            localizedNames.all { it.name.isNotBlank() } &&
            localizedNames.none { it.name.length > MAX_OSM_TAG_VALUE_LENGTH },
        hasChanges = localizedNames.isNotEmpty() && localizedNames.any { it.name.isNotBlank() },
        onClickOk = {
            preferences.preferredLanguageForNames =
                localizedNames.firstOrNull()?.languageTag?.takeIf { it.isNotEmpty() }
            on(Answer(localizedNames))
        },
        modifier = modifier,
        otherAnswers = { otherAnswers() + listOf(
            AnswerItem(stringResource(Res.string.quest_streetName_answer_cantType)) {
                showKeyboardInfo = true
            },
            AnswerItem(stringResource(Res.string.quest_placeName_no_name_answer)) {
                confirmNoName = true
            },
        ) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hint != null) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.body2,
                    LocalContentAlpha provides ContentAlpha.medium
                ) {
                    hint()
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

    if (showKeyboardInfo) {
        InfoDialog(
            onDismissRequest = { showKeyboardInfo = false },
            title = { Text(stringResource(Res.string.quest_streetName_cantType_title)) },
            text = { Text(stringResource(Res.string.quest_streetName_cantType_description2)) }
        )
    }

    if (confirmNoName) {
        AreYouSureDialog(
            onDismissRequest = { confirmNoName = false },
            onConfirmed = { on(Answer(emptyList())) },
            titleText = stringResource(Res.string.quest_name_answer_noName_confirmation_title),
            text = noNameConfirmationText,
            confirmButtonText = stringResource(Res.string.quest_name_noName_confirmation_positive),
        )
    }
}
