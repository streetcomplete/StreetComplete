package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.preferences.Preferences

@Stable
abstract class IntroTutorialViewModel : ViewModel() {
    /** Remember that the user finished the intro tutorial */
    abstract fun markShown()
}

@Stable
class IntroTutorialViewModelImpl(private val prefs: Preferences) : IntroTutorialViewModel() {
    override fun markShown() {
        prefs.hasShownTutorial = true
    }
}
