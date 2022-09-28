package de.westnordost.streetcomplete.screens.main.controls

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnPreDraw
import de.westnordost.streetcomplete.databinding.DialogMainMenuGridBinding
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeDialog
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.UserActivity

/** Shows a dialog containing the main menu items.
 *  This is an almost exact copy of MainMenuDialog, because the compiler doesn't like being able to select
 *  the binding on init. */
class MainMenuGridDialog(
    context: Context,
    indexInTeam: Int?,
    onClickDownload: () -> Unit,
    onEnableTeamMode: (Int, Int) -> Unit,
    onDisableTeamMode: () -> Unit,
    onClickOverlays: () -> Unit,
) : AlertDialog(context) {
    init {
        val binding = DialogMainMenuGridBinding.inflate(LayoutInflater.from(context))

        binding.profileButton.setOnClickListener {
            val intent = Intent(context, UserActivity::class.java)
            context.startActivity(intent)
            dismiss()
        }
        binding.enableTeamModeButton.setOnClickListener {
            TeamModeDialog(context, onEnableTeamMode).show()
            dismiss()
        }
        binding.disableTeamModeButton.setOnClickListener {
            onDisableTeamMode()
            dismiss()
        }
        binding.settingsButton.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
            dismiss()
        }
        binding.aboutButton.setOnClickListener {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
            dismiss()
        }
        binding.downloadButton.setOnClickListener {
            onClickDownload()
            dismiss()
        }

        binding.overlaysButton.setOnClickListener {
            onClickOverlays()
            dismiss()
        }

        if (indexInTeam != null) {
            binding.teamModeColorCircle.setIndexInTeam(indexInTeam)
            binding.bigMenuItemsContainer.removeView(binding.enableTeamModeButton)
        } else {
            binding.bigMenuItemsContainer.removeView(binding.disableTeamModeButton)
        }

        binding.root.doOnPreDraw {
            binding.bigMenuItemsContainer.columnCount = binding.root.width / binding.profileButton.width
        }

        setView(binding.root)
    }
}
