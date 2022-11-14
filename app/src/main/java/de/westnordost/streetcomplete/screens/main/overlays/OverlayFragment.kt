package de.westnordost.streetcomplete.screens.main.overlays

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.databinding.FragmentOverlaySelectionBinding
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialFragment
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.android.ext.android.inject

class OverlayFragment :
    Fragment(R.layout.fragment_overlay_selection),
    OverlaysTutorialFragment.Listener,
    HasTitle {

    override val title: String get() = getString(R.string.select_overlay)
    private val binding by viewBinding(FragmentOverlaySelectionBinding::bind)
    private val selectedOverlayController: SelectedOverlayController by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val prefs: SharedPreferences by inject()

    private var selectedOverlay: Overlay? = selectedOverlayController.selectedOverlay

    /* --------------------------------------- Lifecycle --------------------------------------- */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = OverlaySelectionAdapter()
        adapter.overlays = overlayRegistry
        adapter.selectedOverlay = selectedOverlayController.selectedOverlay
        adapter.onSelectedOverlay = {
            selectedOverlay = it
            changeEnabledOverlayToSelected()
            requireActivity().finish() // switch overlay without an extra confirmation step
        }
        binding.overlaysList.adapter = adapter
        binding.overlaysList.layoutManager = LinearLayoutManager(context)

        binding.applyButton.setOnClickListener {
            changeEnabledOverlayToSelected()
        }

        val hasShownTutorial = prefs.getBoolean(Prefs.HAS_SHOWN_TUTORIAL_FOR_OVERLAYS, false)
        if (!hasShownTutorial) {
            binding.overlaysListContainer.visibility = View.GONE
            binding.fragmentContainerInOverlaySelection.visibility = View.VISIBLE
            childFragmentManager.commit {
                setReorderingAllowed(true)
                setCustomAnimations(R.anim.fade_in_from_bottom, R.anim.fade_out_to_bottom)
                replace(R.id.fragment_container_in_overlay_selection, OverlaysTutorialFragment()) // id.fragment_container ???
                addToBackStack("tutorial")
            }
        }
    }

    override fun onTutorialFinished() {
        binding.fragmentContainerInOverlaySelection.visibility = View.GONE
        binding.overlaysListContainer.visibility = View.VISIBLE
        prefs.edit { putBoolean(Prefs.HAS_SHOWN_TUTORIAL_FOR_OVERLAYS, true) }
    }

    fun changeEnabledOverlayToSelected() {
        selectedOverlayController.selectedOverlay = selectedOverlay
    }
}
