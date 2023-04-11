package de.westnordost.streetcomplete.screens.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.westnordost.streetcomplete.screens.FragmentContainerActivity
import de.westnordost.streetcomplete.screens.settings.questselection.QuestPresetsFragment
import de.westnordost.streetcomplete.screens.settings.questselection.QuestSelectionFragment

class SettingsActivity : FragmentContainerActivity(), SettingsFragment.Listener {
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (savedInstanceState == null) {
            mainFragment = SettingsFragment()
        }
        val launchQuestSettings = intent.getBooleanExtra(EXTRA_LAUNCH_QUEST_SETTINGS, false)
        if (launchQuestSettings) {
            pushMainFragment(QuestSelectionFragment())
        }
    }

    override fun onClickedQuestSelection() {
        pushMainFragment(QuestSelectionFragment())
    }

    override fun onClickedQuestPresets() {
        pushMainFragment(QuestPresetsFragment())
    }

    companion object {
        fun createLaunchQuestSettingsIntent(context: Context) =
            Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_LAUNCH_QUEST_SETTINGS, true)
            }

        private const val EXTRA_LAUNCH_QUEST_SETTINGS = "launch_quest_settings"
    }
}
