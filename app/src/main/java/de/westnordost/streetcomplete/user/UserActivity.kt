package de.westnordost.streetcomplete.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.LoginStatusSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusListener
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Shows all the user information, login etc.
 *  This activity coordinates quite a number of fragments, which all call back to this one. In order
 *  of appearance:
 *  The LoginFragment, the UserFragment (which contains the viewpager with more
 *  fragments) and the "fake" dialogs AchievementInfoFragment and QuestTypeInfoFragment.
 * */
class UserActivity : FragmentContainerActivity(R.layout.activity_user),
    AchievementsFragment.Listener,
    QuestStatisticsFragment.Listener {

    @Inject internal lateinit var loginStatusSource: LoginStatusSource

    private val countryDetailsFragment get() =
        supportFragmentManager.findFragmentById(R.id.countryDetailsFragment) as CountryInfoFragment?

    private val questTypeDetailsFragment get() =
        supportFragmentManager.findFragmentById(R.id.questTypeDetailsFragment) as QuestTypeInfoFragment?

    private val achievementDetailsFragment get() =
        supportFragmentManager.findFragmentById(R.id.achievementDetailsFragment) as AchievementInfoFragment?

    private val loginStatusListener = object : UserLoginStatusListener {
        override fun onLoggedIn() { lifecycleScope.launch { replaceMainFragment(UserFragment()) }}
        override fun onLoggedOut() { lifecycleScope.launch { replaceMainFragment(LoginFragment()) }}
    }

    init {
        Injector.applicationComponent.inject(this)
    }

    /* --------------------------------------- Lifecycle --------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            mainFragment = when {
                intent.getBooleanExtra(EXTRA_LAUNCH_AUTH, false) -> LoginFragment.create(true)
                loginStatusSource.isLoggedIn -> UserFragment()
                else -> LoginFragment.create()
            }
        }
        loginStatusSource.addLoginStatusListener(loginStatusListener)
    }

    override fun onBackPressed() {
        val countryDetailsFragment = countryDetailsFragment
        if (countryDetailsFragment != null && countryDetailsFragment.isShowing) {
            countryDetailsFragment.dismiss()
            return
        }
        val questTypeDetailsFragment = questTypeDetailsFragment
        if (questTypeDetailsFragment != null && questTypeDetailsFragment.isShowing) {
            questTypeDetailsFragment.dismiss()
            return
        }
        val achievementDetailsFragment = achievementDetailsFragment
        if (achievementDetailsFragment != null && achievementDetailsFragment.isShowing) {
            achievementDetailsFragment.dismiss()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        loginStatusSource.removeLoginStatusListener(loginStatusListener)
    }

    /* ---------------------------- AchievementsFragment.Listener ------------------------------- */

    override fun onClickedAchievement(achievement: Achievement, level: Int, achievementBubbleView: View) {
        achievementDetailsFragment?.show(achievement, level, achievementBubbleView)
    }

    /* --------------------------- QuestStatisticsFragment.Listener ----------------------------- */

    override fun onClickedQuestType(questType: QuestType<*>, solvedCount: Int, questBubbleView: View) {
        questTypeDetailsFragment?.show(questType, solvedCount, questBubbleView)
    }

    override fun onClickedCountryFlag(country: String, solvedCount: Int, rank: Int?, countryBubbleView: View) {
        countryDetailsFragment?.show(country, solvedCount, rank, countryBubbleView)
    }

    /* ------------------------------------------------------------------------------------------ */

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

    companion object {
        const val EXTRA_LAUNCH_AUTH = "de.westnordost.streetcomplete.user.launch_auth"
    }
}


