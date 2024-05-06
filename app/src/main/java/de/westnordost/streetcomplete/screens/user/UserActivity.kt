package de.westnordost.streetcomplete.screens.user

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.screens.FragmentContainerActivity
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.user.login.LoginFragment
import de.westnordost.streetcomplete.screens.user.statistics.CountryInfoFragment
import de.westnordost.streetcomplete.screens.user.statistics.EditStatisticsFragment
import de.westnordost.streetcomplete.screens.user.statistics.EditTypeInfoFragment
import de.westnordost.streetcomplete.util.ktx.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows all the user information, login etc.
 *  This activity coordinates quite a number of fragments, which all call back to this one. In order
 *  of appearance:
 *  The LoginFragment, the UserFragment (which contains the viewpager with more
 *  fragments) and the "fake" dialog QuestTypeInfoFragment.
 * */
class UserActivity :
    FragmentContainerActivity(R.layout.activity_user),
    EditStatisticsFragment.Listener {

    private val viewModel by viewModel<UserViewModel>()

    private val countryDetailsFragment get() =
        supportFragmentManager.findFragmentById(R.id.countryDetailsFragment) as CountryInfoFragment?

    private val editTypeDetailsFragment get() =
        supportFragmentManager.findFragmentById(R.id.editTypeDetailsFragment) as EditTypeInfoFragment?

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fragmentManager: FragmentManager, fragment: Fragment) {
            if (fragment.id == R.id.fragment_container && fragment is HasTitle) {
                title = fragment.title
                supportActionBar?.subtitle = fragment.subtitle
            }
        }
    }

    /* --------------------------------------- Lifecycle --------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            replaceMainFragment(when {
                intent.getBooleanExtra(EXTRA_LAUNCH_AUTH, false) -> LoginFragment.create(true)
                viewModel.isLoggedIn.value -> UserFragment()
                else -> LoginFragment.create()
            })
        }

        observe(viewModel.isLoggedIn) { isLoggedIn ->
            replaceMainFragmentAnimated(if (isLoggedIn) UserFragment() else LoginFragment())
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    /* --------------------------- QuestStatisticsFragment.Listener ----------------------------- */

    override fun onClickedEditType(editType: EditType, editCount: Int, questBubbleView: View) {
        editTypeDetailsFragment?.show(editType, editCount, questBubbleView)
    }

    override fun onClickedCountryFlag(country: String, editCount: Int, rank: Int?, countryBubbleView: View) {
        countryDetailsFragment?.show(country, editCount, rank, countryBubbleView)
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun replaceMainFragmentAnimated(fragment: Fragment) {
        replaceMainFragment(fragment) {
            setCustomAnimations(
                R.anim.fade_in_from_bottom, R.anim.fade_out_to_top,
                R.anim.fade_in_from_bottom, R.anim.fade_out_to_top
            )
        }
    }

    companion object {
        const val EXTRA_LAUNCH_AUTH = "de.westnordost.streetcomplete.screens.user.launch_auth"
    }
}
