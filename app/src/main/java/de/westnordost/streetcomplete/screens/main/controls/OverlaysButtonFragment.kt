package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.overlays.OverlaySelectionAdapter
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class OverlaysButtonFragment : Fragment(R.layout.fragment_overlays_button) {

    private val viewModel by viewModel<OverlaysButtonViewModel>()

    interface Listener {
        fun onShowOverlaysTutorial()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnClickListener {
            if (viewModel.hasShownOverlaysTutorial == false) {
                showOverlaysTutorial()
            } else {
                showOverlaysMenu()
            }
        }

        observe(viewModel.selectedOverlay) { overlay ->
            val iconRes = overlay?.icon ?: R.drawable.ic_overlay_black_24dp
            (view as ImageView).setImageResource(iconRes)
        }
    }

    private fun showOverlaysTutorial() {
        listener?.onShowOverlaysTutorial()
    }

    private fun showOverlaysMenu() {
        val adapter = OverlaySelectionAdapter(viewModel.overlays)
        val popupWindow = ListPopupWindow(requireContext())

        popupWindow.setAdapter(adapter)
        popupWindow.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectOverlay(adapter.getItem(position))
            popupWindow.dismiss()
        }
        popupWindow.anchorView = view
        popupWindow.width = resources.dpToPx(240).toInt()
        popupWindow.show()
    }
}
