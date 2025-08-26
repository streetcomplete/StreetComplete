package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.ui.common.localized_name.LocalizedNamesForm
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.koin.android.ext.android.inject

abstract class AAddLocalizedNameForm<T> : AbstractOsmQuestForm<T>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()
    protected lateinit var localizedNames: MutableState<List<LocalizedName>>
    private lateinit var selectableLanguages: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val languages = getSelectableLanguageTags().toMutableList()
        val preferredLanguage = prefs.preferredLanguageForNames
        if (preferredLanguage != null) {
            if (languages.remove(preferredLanguage)) {
                languages.add(0, preferredLanguage)
            }
        }
        selectableLanguages = languages
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            localizedNames = rememberSerializable {
                mutableStateOf(listOf(LocalizedName(countryInfo.language.orEmpty(), "")))
            }

            Column {
                if (showAbbreviationsHint()) {
                    Text(
                        text = stringResource(R.string.quest_streetName_abbreviation_instruction),
                        style = MaterialTheme.typography.caption.copy(
                            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                        )
                    )
                }
                LocalizedNamesForm(
                    localizedNames = localizedNames.value,
                    onChanged = {
                        localizedNames.value = it
                        checkIsFormComplete()
                    },
                    languageTags = selectableLanguages,
                )
            }
        } }
    }

    protected open fun showAbbreviationsHint(): Boolean = false

    protected open fun getSelectableLanguageTags(): List<String> =
        (countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages).distinct()

    final override fun onClickOk() {
        onClickOk(localizedNames.value)

        val firstLanguage = localizedNames.value.firstOrNull()?.languageTag?.takeIf { it.isNotBlank() }
        if (firstLanguage != null) prefs.preferredLanguageForNames = firstLanguage
    }

    abstract fun onClickOk(names: List<LocalizedName>)

    override fun isFormComplete(): Boolean =
        localizedNames.value.isNotEmpty()
        && localizedNames.value.all { it.name.isNotBlank() }

    override fun isRejectingClose(): Boolean =
        localizedNames.value.isNotEmpty()
        && localizedNames.value.any { it.name.isNotBlank() }
}
