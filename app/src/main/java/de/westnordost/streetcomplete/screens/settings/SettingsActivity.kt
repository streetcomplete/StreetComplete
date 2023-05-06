package de.westnordost.streetcomplete.screens.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import de.westnordost.streetcomplete.screens.FragmentContainerActivity

class SettingsActivity : FragmentContainerActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (savedInstanceState == null) {
            replaceMainFragment(TwoPaneSettingsFragment())
        }
    }

    companion object {
        fun createLaunchQuestSettingsIntent(context: Context) =
            Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_LAUNCH_QUEST_SETTINGS, true)
            }

        const val EXTRA_LAUNCH_QUEST_SETTINGS = "launch_quest_settings"
    }
}
