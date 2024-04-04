package de.westnordost.streetcomplete.screens.main.controls

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class OverlaysButtonViewModel : ViewModel() {
    abstract val selectedOverlay: StateFlow<Overlay?>
    abstract val overlays: List<Overlay>

    abstract val hasShownOverlaysTutorial: Boolean

    abstract fun selectOverlay(overlay: Overlay?)
}

class OverlaysButtonViewModelImpl(
    private val selectedOverlayController: SelectedOverlayController,
    private val overlayRegistry: OverlayRegistry,
    private val prefs: ObservableSettings,
) : OverlaysButtonViewModel() {

    override val selectedOverlay = MutableStateFlow(selectedOverlayController.selectedOverlay)
    override val overlays: List<Overlay> get() = overlayRegistry

    override val hasShownOverlaysTutorial: Boolean get() =
        prefs.getBoolean(Prefs.HAS_SHOWN_OVERLAYS_TUTORIAL, false)

    private val selectedOverlaylistener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            selectedOverlay.value = selectedOverlayController.selectedOverlay
        }
    }

    init {
        selectedOverlayController.addListener(selectedOverlaylistener)
    }

    override fun onCleared() {
        selectedOverlayController.removeListener(selectedOverlaylistener)
    }

    override fun selectOverlay(overlay: Overlay?) {
        launch(IO) {
            selectedOverlayController.selectedOverlay = overlay
        }
    }
}
