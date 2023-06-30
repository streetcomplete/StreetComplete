package de.westnordost.streetcomplete.screens.main.controls

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.databinding.DialogMainMenuBinding
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeDialog
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.UserActivity
import de.westnordost.streetcomplete.util.dialogs.showProfileSelectionDialog

/** Shows a dialog containing the main menu items */
class MainMenuDialog(
    context: Context,
    indexInTeam: Int?,
    onClickDownload: () -> Unit,
    onEnableTeamMode: (Int, Int) -> Unit,
    onDisableTeamMode: () -> Unit,
    onClickOverlays: () -> Unit,
    prefs: SharedPreferences,
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

        binding.overlaysButton.setOnClickListener {
            onClickOverlays()
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

        binding.switchPresetButton.isGone = !prefs.getBoolean(Prefs.MAIN_MENU_SWITCH_PRESETS, false)
        binding.switchPresetButton.setOnClickListener { showProfileSelectionDialog(context, questPresetsController, prefs) }

        setView(binding.root)
    }
}
