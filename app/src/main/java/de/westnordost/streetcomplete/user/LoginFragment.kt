package de.westnordost.streetcomplete.user

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import de.westnordost.osmapi.user.Permission
import de.westnordost.streetcomplete.BackPressedListener
import de.westnordost.streetcomplete.HasTitle
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.OsmApiModule
import de.westnordost.streetcomplete.data.PermissionsApi
import de.westnordost.streetcomplete.data.quest.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.ktx.childFragmentManagerOrNull
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.settings.OAuthFragment
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.*
import oauth.signpost.OAuthConsumer
import javax.inject.Inject

/** Shows only a login button and a text that clarifies that login is necessary for publishing the
 *  answers. */
class LoginFragment : Fragment(R.layout.fragment_login),
    CoroutineScope by CoroutineScope(Dispatchers.Main),
    HasTitle,
    BackPressedListener,
    OAuthFragment.Listener {

    @Inject internal lateinit var unsyncedChangesCountSource: UnsyncedChangesCountSource
    @Inject internal lateinit var userController: UserController

    override val title: String get() = getString(R.string.user_login)

    private val oAuthFragment: OAuthFragment? get() =
        childFragmentManagerOrNull?.findFragmentById(R.id.oauthFragmentContainer) as? OAuthFragment

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginButton.setOnClickListener { pushOAuthFragment() }

        val launchAuth = arguments?.getBoolean(ARG_LAUNCH_AUTH, false) ?: false
        if (launchAuth) {
            pushOAuthFragment()
        }
    }

    override fun onStart() {
        super.onStart()

        val unsyncedChanges = unsyncedChangesCountSource.count
        unpublishedQuestsText.text = getString(R.string.unsynced_quests_not_logged_in_description, unsyncedChanges)
        unpublishedQuestsText.visibility = if (unsyncedChanges > 0) View.VISIBLE else View.GONE
    }

    override fun onBackPressed(): Boolean {
        val f = oAuthFragment
        if (f != null) {
            if(f.onBackPressed()) return true
            childFragmentManager.popBackStack("oauth", POP_BACK_STACK_INCLUSIVE)
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    /* ------------------------------- OAuthFragment.Listener ----------------------------------- */

    override fun onOAuthSuccess(consumer: OAuthConsumer) {
        loginButton.visibility = View.INVISIBLE
        loginProgress.visibility = View.VISIBLE
        childFragmentManager.popBackStack("oauth", POP_BACK_STACK_INCLUSIVE)
        launch {
            if (hasRequiredPermissions(consumer)) {
                userController.logIn(consumer)
            } else {
                context?.toast(R.string.oauth_failed_permissions, Toast.LENGTH_LONG)
                loginButton.visibility = View.VISIBLE
            }
            loginProgress.visibility = View.INVISIBLE
        }
    }

    override fun onOAuthFailed(e: Exception?) {
        childFragmentManager.popBackStack("oauth", POP_BACK_STACK_INCLUSIVE)
        userController.logOut()
    }

    suspend fun hasRequiredPermissions(consumer: OAuthConsumer): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val permissionsApi = PermissionsApi( OsmApiModule.osmConnection(consumer))
                permissionsApi.get().containsAll(REQUIRED_OSM_PERMISSIONS)
            }
            catch (e: Exception) { false }
            }
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun pushOAuthFragment() {
        childFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
            .replace(R.id.oauthFragmentContainer, OAuthFragment())
            .addToBackStack("oauth")
            .commit()
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
