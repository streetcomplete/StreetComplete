package de.westnordost.streetcomplete.screens.settings.language_selection

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readYaml
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class LanguageSelectionViewModel : ViewModel() {
    abstract val selectableLanguages: StateFlow<List<String>?>
    abstract val selectedLanguage: StateFlow<String?>

    abstract fun setSelectedLanguage(value: String?)
}

@Stable
class LanguageSelectionViewModelImpl(
    private val prefs: Preferences,
    private val res: Res,
) : LanguageSelectionViewModel() {

    override val selectableLanguages = MutableStateFlow<List<String>?>(null)
    override val selectedLanguage = MutableStateFlow(prefs.language)

    private val languageChangedListener: SettingsListener

    init {
        languageChangedListener = prefs.onLanguageChanged { selectedLanguage.value = it }
        launch {
            selectableLanguages.value = res.readYaml<List<String>>("files/languages.yml")
        }
    }

    override fun onCleared() {
        languageChangedListener.deactivate()
    }

    override fun setSelectedLanguage(value: String?) { prefs.language = value }
}
