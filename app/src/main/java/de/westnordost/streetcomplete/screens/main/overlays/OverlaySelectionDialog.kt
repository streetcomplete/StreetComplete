package de.westnordost.streetcomplete.screens.main.overlays

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.databinding.DialogOverlaySelectionBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Dialog in which the user selects which overlay to display */
class OverlaySelectionDialog(context: Context) : AlertDialog(context), KoinComponent {

    private val selectedOverlayController: SelectedOverlayController by inject()
    private val overlayRegistry: OverlayRegistry by inject()

    init {
        val currentOverlay = selectedOverlayController.selectedOverlay

        val adapter = OverlaySelectionAdapter()
        adapter.overlays = overlayRegistry
        adapter.selectedOverlay = currentOverlay
        adapter.onSelectedOverlay = { selectedOverlay ->
            if (currentOverlay != selectedOverlay) {
                selectedOverlayController.selectedOverlay = selectedOverlay
            }
            dismiss()
        }

        val binding = DialogOverlaySelectionBinding.inflate(LayoutInflater.from(context))
        binding.overlaysList.adapter = adapter
        binding.overlaysList.layoutManager = LinearLayoutManager(context)

        setTitle(R.string.select_overlay)

        setButton(BUTTON_NEGATIVE, context.resources.getText(android.R.string.cancel)) { _, _ ->
            dismiss()
        }

        setView(binding.root)
    }
}
