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

    // TODO maybe this should be used instead to swap between tutorial and actual menu?
    //   not the current solution? Or something else is the proper way to achieve this?
    private fun replaceMainFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack("main", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.fade_in_from_bottom, R.anim.fade_out_to_top,
                R.anim.fade_in_from_bottom, R.anim.fade_out_to_top
            )
            replace(R.id.fragment_container, fragment)
        }
    }
}
