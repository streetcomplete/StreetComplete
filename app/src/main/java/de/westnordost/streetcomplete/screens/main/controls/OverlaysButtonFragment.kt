package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.screens.main.overlays.OverlaySelectionAdapter
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.prefs.Preferences
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class OverlaysButtonFragment : Fragment(R.layout.fragment_overlays_button) {

    private val selectedOverlayController: SelectedOverlayController by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val prefs: Preferences by inject()

    private val selectedOverlaylistener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            viewLifecycleScope.launch { updateOverlayButtonIcon() }
        }
    }

    interface Listener {
        fun onShowOverlaysTutorial()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener { onClickButton() }
    }

    override fun onStart() {
        super.onStart()
        updateOverlayButtonIcon()
        selectedOverlayController.addListener(selectedOverlaylistener)
    }

    override fun onStop() {
        super.onStop()
        selectedOverlayController.removeListener(selectedOverlaylistener)
    }

    private fun onClickButton() {
        val hasShownTutorial = prefs.getBoolean(Prefs.HAS_SHOWN_OVERLAYS_TUTORIAL, false)
        if (!hasShownTutorial) {
            showOverlaysTutorial()
        } else {
            showOverlaysMenu()
        }
    }

    private fun showOverlaysTutorial() {
        listener?.onShowOverlaysTutorial()
    }

    private fun showOverlaysMenu() {
        val adapter = OverlaySelectionAdapter(overlayRegistry)
        val popupWindow = ListPopupWindow(requireContext())

        popupWindow.setAdapter(adapter)
        popupWindow.setOnItemClickListener { _, _, position, _ ->
            selectedOverlayController.selectedOverlay = adapter.getItem(position)
            popupWindow.dismiss()
        }
        popupWindow.anchorView = view
        popupWindow.width = requireContext().dpToPx(240).toInt()
        popupWindow.show()
    }

    private fun updateOverlayButtonIcon() {
        val iconRes = selectedOverlayController.selectedOverlay?.icon ?: R.drawable.ic_overlay_black_24dp
        (view as ImageView).setImageResource(iconRes)
    }
}
