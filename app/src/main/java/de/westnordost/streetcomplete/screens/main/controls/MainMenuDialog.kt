package de.westnordost.streetcomplete.screens.main.controls

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.databinding.DialogMainMenuBinding
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeDialog
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.UserActivity
import de.westnordost.streetcomplete.util.dialogs.showProfileSelectionDialog
import de.westnordost.streetcomplete.util.prefs.Preferences

/** Shows a dialog containing the main menu items */
class MainMenuDialog(
    context: Context,
    indexInTeam: Int?,
    onClickDownload: () -> Unit,
    onEnableTeamMode: (Int, Int) -> Unit,
    onDisableTeamMode: () -> Unit,
    prefs: Preferences,
    questPresetsController: QuestPresetsController,
) : AlertDialog(context) {
    init {
        val binding = DialogMainMenuBinding.inflate(LayoutInflater.from(context))

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
        binding.enableTeamModeButtonGrid.setOnClickListener {
            TeamModeDialog(context, onEnableTeamMode).show()
            dismiss()
        }
        binding.disableTeamModeButtonGrid.setOnClickListener {
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
        binding.aboutButton.text = binding.aboutButton.text.toString() + " SCEE"
        binding.downloadButton.setOnClickListener {
            onClickDownload()
            dismiss()
        }
        binding.downloadButtonGrid.setOnClickListener {
            onClickDownload()
            dismiss()
        }

        if (indexInTeam != null) {
            binding.teamModeColorCircle.setIndexInTeam(indexInTeam)
        }
        binding.enableTeamModeButton.isGone = indexInTeam != null
        binding.disableTeamModeButton.isGone = indexInTeam == null

        binding.root.doOnPreDraw {
            binding.bigMenuItemsContainer.columnCount = binding.root.width / binding.profileButton.width
        }

        if (prefs.getBoolean(Prefs.MAIN_MENU_FULL_GRID, false)) {
            binding.downloadButtonGrid.isVisible = true
            binding.downloadButton.isGone = true
            binding.enableTeamModeButtonGrid.isVisible = indexInTeam == null
            binding.disableTeamModeButtonGrid.isVisible = indexInTeam != null
            binding.enableTeamModeButtonGrid.isGone = indexInTeam != null
            binding.disableTeamModeButtonGrid.isGone = indexInTeam == null
            binding.enableTeamModeButton.isGone = true
            binding.disableTeamModeButton.isGone = true
            binding.divider.isGone = !prefs.getBoolean(Prefs.MAIN_MENU_SWITCH_PRESETS, false)
        } else {
            binding.downloadButtonGrid.isGone = true
            binding.enableTeamModeButtonGrid.isGone = true
            binding.disableTeamModeButtonGrid.isGone = true
        }

        binding.switchPresetButton.isGone = !prefs.getBoolean(Prefs.MAIN_MENU_SWITCH_PRESETS, false)
        binding.switchPresetButton.setOnClickListener {
            showProfileSelectionDialog(context, questPresetsController, prefs)
            dismiss()
        }

        setView(binding.root)
    }
}
