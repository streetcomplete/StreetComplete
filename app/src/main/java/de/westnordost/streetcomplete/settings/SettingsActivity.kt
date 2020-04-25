package de.westnordost.streetcomplete.settings

import android.os.Bundle
import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.settings.questselection.QuestSelectionFragment

class SettingsActivity : FragmentContainerActivity(), SettingsFragment.Listener
{
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (savedInstanceState == null) {
            mainFragment = SettingsFragment()
        }
    }

    override fun onClickedQuestSelection() {
        pushMainFragment(QuestSelectionFragment())
    }
}
