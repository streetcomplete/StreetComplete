package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.Abbreviations
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLanguage
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.ui.common.localized_name.LocalizedNamesForm
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.localized_name.confirmPossibleAbbreviationsIfAny
import de.westnordost.streetcomplete.view.localized_name.getPossibleAbbreviations
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class AAddLocalizedNameForm<T> : AbstractOsmQuestForm<T>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)

    private val prefs: Preferences by inject()
    protected lateinit var localizedNames: MutableState<List<LocalizedName>>
    protected var abbreviationsByLanguage: Map<String, Abbreviations?> = emptyMap()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectableLanguages = getSelectableLanguageTags().toMutableList()
        val preferredLanguage = prefs.preferredLanguageForNames
        if (preferredLanguage != null) {
            if (selectableLanguages.remove(preferredLanguage)) {
                selectableLanguages.add(0, preferredLanguage)
            }
        }

        val abbrs = getAbbreviationsByLanguage()
        if (abbrs != null) {
            viewLifecycleScope.launch {
                abbreviationsByLanguage = selectableLanguages.associateWith { abbrs[it] }
            }
        }

        binding.composeViewBase.content { Surface {
            localizedNames = remember {
                mutableStateOf(listOf(LocalizedName(countryInfo.language.orEmpty(), "")))
            }

            LocalizedNamesForm(
                localizedNames = localizedNames.value,
                onChanged = {
                    localizedNames.value = it
                    checkIsFormComplete()
                },
                languageTags = selectableLanguages,
                abbreviationsByLanguage = abbreviationsByLanguage,
            )
        } }
    }

    protected open fun getSelectableLanguageTags(): List<String> =
        (countryInfo.officialLanguages + countryInfo.additionalStreetsignLanguages).distinct()

    protected open fun getAbbreviationsByLanguage(): AbbreviationsByLanguage? = null

    final override fun onClickOk() {
        val possibleAbbreviations = ArrayDeque(getPossibleAbbreviations(
            localizedNames = localizedNames.value,
            defaultLanguage = countryInfo.language,
            abbreviationsByLanguage = abbreviationsByLanguage
        ))

        confirmPossibleAbbreviationsIfAny(requireContext(), possibleAbbreviations) {
            onClickOk(localizedNames.value)
        }

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
