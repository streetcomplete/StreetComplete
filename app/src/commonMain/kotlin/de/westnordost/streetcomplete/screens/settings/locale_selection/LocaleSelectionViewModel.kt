package de.westnordost.streetcomplete.screens.settings.locale_selection

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readYaml
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class LocaleSelectionViewModel : ViewModel() {
    abstract val selectableLocales: StateFlow<List<Locale>?>
    abstract val selectedLocale: StateFlow<Locale?>

    abstract fun setSelectedLocale(value: Locale?)
}

@Stable
class LocaleSelectionViewModelImpl(
    private val prefs: Preferences,
    private val res: Res,
) : LocaleSelectionViewModel() {

    override val selectableLocales = MutableStateFlow<List<Locale>?>(null)
    override val selectedLocale = MutableStateFlow(prefs.locale)

    private val localeChangedListener: SettingsListener

    init {
        localeChangedListener = prefs.onLocaleChanged { selectedLocale.value = it }
        launch {
            selectableLocales.value = res
                .readYaml<List<String>>("files/languages.yml")
                .map { Locale(it) }
        }
    }

    override fun onCleared() {
        localeChangedListener.deactivate()
    }

    override fun setSelectedLocale(value: Locale?) { prefs.locale = value }
}
