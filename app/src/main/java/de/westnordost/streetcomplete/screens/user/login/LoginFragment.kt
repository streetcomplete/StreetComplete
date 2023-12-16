package de.westnordost.streetcomplete.screens.user.login

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.databinding.FragmentLoginBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Shows only a login button and a text that clarifies that login is necessary for publishing the
 *  answers. */
class LoginFragment :
    Fragment(R.layout.fragment_login),
    HasTitle,
    OAuthFragment.Listener {

    private val unsyncedChangesCountSource: UnsyncedChangesCountSource by inject()
    private val userLoginStatusController: UserLoginStatusController by inject()
    private val userUpdater: UserUpdater by inject()

    override val title: String get() = getString(R.string.user_login)

    private val binding by viewBinding(FragmentLoginBinding::bind)

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
            binding.unpublishedEditCountText.text = getString(R.string.unsynced_quests_not_logged_in_description, unsyncedChanges)
            binding.unpublishedEditCountText.isGone = unsyncedChanges <= 0
        }
    }

    /* ------------------------------- OAuthFragment.Listener ----------------------------------- */

    override fun onOAuthSuccess(accessToken: String) {
        binding.loginButton.visibility = View.INVISIBLE
        binding.loginProgress.visibility = View.VISIBLE
        childFragmentManager.popBackStack("oauth", POP_BACK_STACK_INCLUSIVE)
        userLoginStatusController.logIn(accessToken)
        userUpdater.update()
        binding.loginProgress.visibility = View.INVISIBLE
    }

    override fun onOAuthFailed(e: Exception?) {
        childFragmentManager.popBackStack("oauth", POP_BACK_STACK_INCLUSIVE)
        userLoginStatusController.logOut()
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

        private const val ARG_LAUNCH_AUTH = "launch_auth"
    }
}
