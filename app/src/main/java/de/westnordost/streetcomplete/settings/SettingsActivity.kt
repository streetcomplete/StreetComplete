package de.westnordost.streetcomplete.settings

import android.os.Bundle
import de.westnordost.streetcomplete.FragmentContainerActivity
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.oauth.OsmOAuthDialogFragment
import oauth.signpost.OAuthConsumer
import javax.inject.Inject


class SettingsActivity : FragmentContainerActivity(), OsmOAuthDialogFragment.Listener {

    @Inject lateinit var userController: UserController

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra(EXTRA_LAUNCH_AUTH, false)) {
            OsmOAuthDialogFragment().show(supportFragmentManager, OsmOAuthDialogFragment.TAG)
        }
        intent.putExtra(EXTRA_FRAGMENT_CLASS, SettingsFragment::class.java.name)
    }

    override fun onOAuthSuccess(consumer: OAuthConsumer) {
        userController.logIn(consumer)
    }

    override fun onOAuthFailed(e: Exception?) {
        userController.logOut()
    }

    companion object {
        const val EXTRA_LAUNCH_AUTH = "de.westnordost.streetcomplete.settings.launch_auth"
    }
}
