package de.westnordost.streetcomplete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.preferences.Preferences
import kotlinx.coroutines.flow.MutableStateFlow

/** Application preferences and incoming requests, independent of the visible destination. */
class AppViewModel(prefs: Preferences, private val savedState: SavedStateHandle) : ViewModel() {
    val theme = MutableStateFlow(prefs.theme)
    val language = MutableStateFlow(prefs.language)
    val keepScreenOn = MutableStateFlow(prefs.keepScreenOn)

    val pendingUri = savedState.getStateFlow<String?>("uri", null)
    val openSettings = savedState.getStateFlow("settings", false)

    private val listeners = listOf(
        prefs.onThemeChanged { theme.value = it },
        prefs.onLanguageChanged { language.value = it },
        prefs.onKeepScreenOnChanged { keepScreenOn.value = it },
    )

    fun openUri(uri: String) { savedState["uri"] = uri }
    fun showSettings() { savedState["settings"] = true }
    fun consumeUri() { savedState["uri"] = null }
    fun consumeSettingsRequest() { savedState["settings"] = false }

    override fun onCleared() { listeners.forEach { it.deactivate() } }
}
