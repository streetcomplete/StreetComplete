package de.westnordost.streetcomplete.screens.main.overlays

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.databinding.DialogOverlaySelectionBinding
import de.westnordost.streetcomplete.overlays.Overlay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Dialog in which the user selects which overlay to display */
class OverlaySelectionDialog(context: Context) : AlertDialog(context), KoinComponent {

    private val selectedOverlayController: SelectedOverlayController by inject()
    private val overlayRegistry: OverlayRegistry by inject()

    private val binding = DialogOverlaySelectionBinding.inflate(LayoutInflater.from(context))
    private var selectedOverlay: Overlay? = selectedOverlayController.selectedOverlay

    init {
        val adapter = OverlaySelectionAdapter()
        adapter.overlays = overlayRegistry
        adapter.selectedOverlay = selectedOverlayController.selectedOverlay
        adapter.onSelectedOverlay = { selectedOverlay = it }
        binding.overlaysList.adapter = adapter
        binding.overlaysList.layoutManager = LinearLayoutManager(context)

        setTitle(R.string.select_overlay)

        setButton(BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
            selectedOverlayController.selectedOverlay = selectedOverlay
            dismiss()
        }

        setView(binding.root)
    }
}
