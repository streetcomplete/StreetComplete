package de.westnordost.streetcomplete.screens.user.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.ui.util.composableContent
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(), HasTitle {

    override val title: String get() = getString(R.string.user_login)

    private val viewModel by viewModel<LoginViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        composableContent {
            Surface {
                LoginScreen(viewModel)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val launchAuth = arguments?.getBoolean(ARG_LAUNCH_AUTH, false) ?: false
        if (launchAuth) {
            viewModel.startLogin()
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
