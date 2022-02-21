package de.westnordost.streetcomplete.user

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import de.westnordost.osmapi.user.Permission
import de.westnordost.osmapi.user.PermissionsApi
import de.westnordost.streetcomplete.BackPressedListener
import de.westnordost.streetcomplete.HasTitle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.osmConnection
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.databinding.FragmentLoginBinding
import de.westnordost.streetcomplete.ktx.childFragmentManagerOrNull
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.settings.OAuthFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import oauth.signpost.OAuthConsumer
import org.koin.android.ext.android.inject

/** Shows only a login button and a text that clarifies that login is necessary for publishing the
 *  answers. */
class LoginFragment :
    Fragment(R.layout.fragment_login),
    HasTitle,
    BackPressedListener,
    OAuthFragment.Listener {

    private val unsyncedChangesCountSource: UnsyncedChangesCountSource by inject()
    private val userLoginStatusController: UserLoginStatusController by inject()
    private val userUpdater: UserUpdater by inject()

    override val title: String get() = getString(R.string.user_login)

    private val binding by viewBinding(FragmentLoginBinding::bind)

    private val oAuthFragment: OAuthFragment? get() =
        childFragmentManagerOrNull?.findFragmentById(R.id.oauthFragmentContainer) as? OAuthFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener { pushOAuthFragment() }

        val launchAuth = arguments?.getBoolean(ARG_LAUNCH_AUTH, false) ?: false
        if (launchAuth) {
            pushOAuthFragment()
        }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleScope.launch {
            val unsyncedChanges = unsyncedChangesCountSource.getCount()
            binding.unpublishedQuestsText.text = getString(R.string.unsynced_quests_not_logged_in_description, unsyncedChanges)
            binding.unpublishedQuestsText.isGone = unsyncedChanges <= 0
        }
    }

    override fun onBackPressed(): Boolean {
        val f = oAuthFragment
        if (f != null) {
            if (f.onBackPressed()) return true
            childFragmentManager.popBackStack("oauth", POP_BACK_STACK_INCLUSIVE)
            return true
        }
        return false
    }

    /* ------------------------------- OAuthFragment.Listener ----------------------------------- */

    override fun onOAuthSuccess(consumer: OAuthConsumer) {
        binding.loginButton.visibility = View.INVISIBLE
        binding.loginProgress.visibility = View.VISIBLE
        childFragmentManager.popBackStack("oauth", POP_BACK_STACK_INCLUSIVE)
        viewLifecycleScope.launch {
            if (hasRequiredPermissions(consumer)) {
                userLoginStatusController.logIn(consumer)
                userUpdater.update()
            } else {
                context?.toast(R.string.oauth_failed_permissions, Toast.LENGTH_LONG)
                binding.loginButton.visibility = View.VISIBLE
            }
            binding.loginProgress.visibility = View.INVISIBLE
        }
    }

    override fun onOAuthFailed(e: Exception?) {
        childFragmentManager.popBackStack("oauth", POP_BACK_STACK_INCLUSIVE)
        userLoginStatusController.logOut()
    }

    suspend fun hasRequiredPermissions(consumer: OAuthConsumer): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                /* we didn't save the new OAuthConsumer yet but we want to make an API call with it
                   to check if the user granted all required permissions, this is why we need to
                   create a new OsmConnection with the supplied consumer instead of using an
                   injected one */
                val permissionsApi = PermissionsApi(osmConnection(consumer))
                permissionsApi.get().containsAll(REQUIRED_OSM_PERMISSIONS)
            } catch (e: Exception) { false }
        }
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun pushOAuthFragment() {
        childFragmentManager.commit {
            setCustomAnimations(
                R.anim.enter_from_end, R.anim.exit_to_start,
                R.anim.enter_from_start, R.anim.exit_to_end
            )
            replace<OAuthFragment>(R.id.oauthFragmentContainer)
            addToBackStack("oauth")
        }
    }

    companion object {
        fun create(launchAuth: Boolean = false): LoginFragment {
            val f = LoginFragment()
            f.arguments = bundleOf(ARG_LAUNCH_AUTH to launchAuth)
            return f
        }

        private val REQUIRED_OSM_PERMISSIONS = listOf(
            Permission.READ_PREFERENCES_AND_USER_DETAILS,
            Permission.MODIFY_MAP,
            Permission.WRITE_NOTES
        )

        private const val ARG_LAUNCH_AUTH = "launch_auth"
    }
}
