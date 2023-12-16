package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.overlays.custom.getCustomOverlayIndices
import de.westnordost.streetcomplete.overlays.custom.getIndexedCustomOverlayPref
import de.westnordost.streetcomplete.screens.main.overlays.OverlaySelectionAdapter
import de.westnordost.streetcomplete.util.getFakeCustomOverlays
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.showOverlayCustomizer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class OverlaysButtonFragment : Fragment(R.layout.fragment_overlays_button) {

    private val selectedOverlayController: SelectedOverlayController by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
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
        val fakeOverlays = getFakeCustomOverlays(prefs, requireContext())
        val overlays =  overlayRegistry.filter {
            val eeAllowed = if (prefs.getBoolean(Prefs.EXPERT_MODE, false)) true
                else overlayRegistry.getOrdinalOf(it)!! < ApplicationConstants.EE_QUEST_OFFSET
            eeAllowed && it !is CustomOverlay
        } + fakeOverlays
        val adapter = OverlaySelectionAdapter(overlays, prefs, questTypeRegistry)
        val popupWindow = ListPopupWindow(requireContext())
        popupWindow.isModal = true // with this the popup is dismissed on back button

        popupWindow.setAdapter(adapter)
        popupWindow.setOnItemClickListener { _, _, position, _ ->
            var selectedOverlay = adapter.getItem(position)
            if (selectedOverlay?.title == 0) {
                prefs.putInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, selectedOverlay.wikiLink!!.toInt())
                // set the actual custom overlay instead of the fake one
                selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
            }
            if (selectedOverlay == null && position != 0) {
                val newIdx = if (prefs.getString(Prefs.CUSTOM_OVERLAY_INDICES, "0").isNullOrBlank()) 0
                    else getCustomOverlayIndices(prefs).max() + 1
                showOverlayCustomizer(newIdx, requireContext(), prefs, questTypeRegistry, {
                    selectedOverlayController.selectedOverlay = overlayRegistry.getByName(CustomOverlay::class.simpleName!!)
                }, {
                    // do nothing if deleted (should not be possible)
                })
            }
            if (selectedOverlayController.selectedOverlay != selectedOverlay || selectedOverlay is CustomOverlay)
                selectedOverlayController.selectedOverlay = selectedOverlay // only set same overlay if it's custom to avoid unnecessarily reloading stuff

            popupWindow.dismiss()
        }
        popupWindow.anchorView = view
        popupWindow.width = requireContext().dpToPx(240).toInt()
        popupWindow.show()
    }

    private fun updateOverlayButtonIcon() {
        val overlay = selectedOverlayController.selectedOverlay
        val iconRes = if (overlay is CustomOverlay) {
            requireContext().resources.getIdentifier(
                prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)), "ic_custom_overlay"),
                "drawable", requireContext().packageName
            ).takeIf { it != 0 } ?: R.drawable.ic_custom_overlay
        } else
            overlay?.icon ?: R.drawable.ic_overlay_black_24dp
        (view as ImageView).setImageResource(iconRes)
    }
}
