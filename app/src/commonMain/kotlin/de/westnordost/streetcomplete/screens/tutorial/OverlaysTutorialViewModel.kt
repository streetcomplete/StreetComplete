package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.preferences.Preferences

@Stable
abstract class OverlaysTutorialViewModel : ViewModel() {
    /** Remember that the user finished the overlays tutorial */
    abstract fun markShown()
}

@Stable
class OverlaysTutorialViewModelImpl(private val prefs: Preferences) : OverlaysTutorialViewModel() {
    override fun markShown() {
        prefs.hasShownOverlaysTutorial = true
    }
}
