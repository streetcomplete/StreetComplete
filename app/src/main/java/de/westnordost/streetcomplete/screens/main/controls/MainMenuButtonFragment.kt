package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.databinding.FragmentMainMenuButtonBinding
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.popIn
import de.westnordost.streetcomplete.util.ktx.popOut
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Fragment that shows the main menu button and manages its logic */
class MainMenuButtonFragment : Fragment(R.layout.fragment_main_menu_button) {

    interface Listener {
        fun getDownloadArea(): BoundingBox?
    }

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val viewModel by viewModel<MainMenuButtonViewModel>()
    private val binding by viewBinding(FragmentMainMenuButtonBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mainMenuButton.setOnClickListener { onClickMainMenu() }

        observe(viewModel.isTeamMode) { isTeamMode ->
            if (isTeamMode) {
                // always show this toast on start to remind user that it is still on
                context?.toast(R.string.team_mode_active)
                binding.teamModeColorCircle.popIn()
                binding.teamModeColorCircle.setIndexInTeam(viewModel.indexInTeam)
            } else {
                // show this only once when turning it off
                if (viewModel.teamModeChanged) context?.toast(R.string.team_mode_deactivated)
                binding.teamModeColorCircle.popOut()
            }
            viewModel.teamModeChanged = false
        }
    }

    internal fun onClickMainMenu() {
        MainMenuDialog(
            requireContext(),
            if (viewModel.isTeamMode.value) viewModel.indexInTeam else null,
            this::onClickDownload,
            viewModel::enableTeamMode,
            viewModel::disableTeamMode
        ).show()
    }

    private fun onClickDownload() {
        if (viewModel.isConnected) {
            downloadDisplayedArea()
        } else {
            context?.toast(R.string.offline)
        }
    }

    private fun downloadDisplayedArea() {
        val downloadArea = listener?.getDownloadArea() ?: return

        if (viewModel.isUserInitiatedDownloadInProgress) {
            context?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.confirmation_cancel_prev_download_title)
                    .setPositiveButton(R.string.confirmation_cancel_prev_download_confirmed) { _, _ ->
                        viewModel.download(downloadArea)
                    }
                    .setNegativeButton(R.string.confirmation_cancel_prev_download_cancel, null)
                    .show()
            }
        } else {
            viewModel.download(downloadArea)
        }
    }
}
