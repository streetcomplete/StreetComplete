package de.westnordost.streetcomplete.settings

import android.os.Bundle
import android.widget.Toast
import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.ktx.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import oauth.signpost.OAuthConsumer
import javax.inject.Inject


class SettingsActivity :
        FragmentContainerActivity(),
        OAuthFragment.Listener,
        CoroutineScope by CoroutineScope(Dispatchers.Main)
{

    @Inject lateinit var userController: UserController

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra(EXTRA_LAUNCH_AUTH, false)) {
            currentFragment =
                OAuthFragment()
        }
        intent.putExtra(EXTRA_FRAGMENT_CLASS, SettingsFragment::class.java.name)
    }

    override fun onOAuthSuccess(consumer: OAuthConsumer) {
        launch {
            if (userController.hasRequiredPermissions(consumer)) {
                toast(R.string.pref_title_authorized_summary, Toast.LENGTH_LONG)
                userController.logIn(consumer)
            } else {
                toast(R.string.oauth_failed_permissions, Toast.LENGTH_LONG)
            }
            supportFragmentManager.popBackStack()
        }
    }

    override fun onOAuthFailed(e: Exception?) {
        userController.logOut()
        supportFragmentManager.popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    companion object {
        const val EXTRA_LAUNCH_AUTH = "de.westnordost.streetcomplete.settings.launch_auth"
    }
}
