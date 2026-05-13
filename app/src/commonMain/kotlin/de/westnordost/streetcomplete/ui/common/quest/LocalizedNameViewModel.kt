package de.westnordost.streetcomplete.ui.common.quest

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

class LocalizedNameViewModel(
    private val prefs: Preferences,
) : ViewModel() {

    fun getLanguagesWithPreferredFirst(languages: List<String>): List<String> {
        val languages = languages.distinct().toMutableList()
        val preferredLanguageTag = prefs.preferredLanguageForNames
        if (preferredLanguageTag != null) {
            if (languages.remove(preferredLanguageTag)) {
                languages.add(0, preferredLanguageTag)
            }
        }
        return languages
    }

    fun savePreferredLanguage(localizedNames: List<LocalizedName>) {
        val firstLanguage = localizedNames
            .firstOrNull()
            ?.languageTag
            ?.takeIf { it.isNotEmpty() }
        if (firstLanguage != null) {
            prefs.preferredLanguageForNames = firstLanguage
        }
    }
}
