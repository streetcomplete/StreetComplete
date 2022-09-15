package de.westnordost.streetcomplete.screens.main.overlays

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.user.UserLoginStatusSource
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.screens.FragmentContainerActivity
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialFragment
import de.westnordost.streetcomplete.screens.user.UserFragment
import de.westnordost.streetcomplete.screens.user.achievements.AchievementInfoFragment
import de.westnordost.streetcomplete.screens.user.achievements.AchievementsFragment
import de.westnordost.streetcomplete.screens.user.login.LoginFragment
import de.westnordost.streetcomplete.screens.user.statistics.CountryInfoFragment
import de.westnordost.streetcomplete.screens.user.statistics.EditStatisticsFragment
import de.westnordost.streetcomplete.screens.user.statistics.EditTypeInfoFragment
import de.westnordost.streetcomplete.view.dialogs.RequestLoginDialog
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class OverlaySelectionActivity :
    //     FragmentContainerActivity(R.layout.dialog_overlay_selection),
    FragmentContainerActivity(R.layout.activity_user) { // TODO how this even works? This layout goes to AppCompatActivity but is apparently somehow ignored and does not matter?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            mainFragment = OverlayFragment()
        }
    }
}
