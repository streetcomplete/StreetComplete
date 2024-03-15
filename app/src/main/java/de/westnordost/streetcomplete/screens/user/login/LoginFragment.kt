package de.westnordost.streetcomplete.screens.user.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentLoginBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.user.login.LoginError.*
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/** Leads user through the OAuth 2 auth flow to login */
class LoginFragment : Fragment(R.layout.fragment_login), HasTitle {

    override val title: String get() = getString(R.string.user_login)

    private val viewModel by viewModel<LoginViewModel>()
    private val binding by viewBinding(FragmentLoginBinding::bind)

    private val webViewClient: OAuthWebViewClient = OAuthWebViewClient()

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            binding.webView.goBack()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        binding.webView.settings.userAgentString = ApplicationConstants.USER_AGENT
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.allowContentAccess = true
        binding.webView.settings.setSupportZoom(false)
        binding.webView.webViewClient = webViewClient

        binding.loginButton.setOnClickListener { viewModel.startLogin() }

        val launchAuth = arguments?.getBoolean(ARG_LAUNCH_AUTH, false) ?: false
        if (launchAuth) {
            viewModel.startLogin()
        }

        observe(viewModel.unsyncedChangesCount) { count ->
            binding.unpublishedEditCountText.text = getString(R.string.unsynced_quests_not_logged_in_description, count)
            binding.unpublishedEditCountText.isGone = count <= 0
        }
        observe(viewModel.loginState) { state ->
            binding.loginButtonContainer.isInvisible = state !is LoggedOut
            binding.webView.isInvisible = state !is RequestingAuthorization
            // fragment is dismissed on login, so while it is still there, show progress spinner
            binding.progressView.isInvisible = state !is RetrievingAccessToken && state !is LoggedIn

            if (state is RequestingAuthorization) {
                binding.webView.loadUrl(
                    viewModel.authorizationRequestUrl,
                    mutableMapOf("Accept-Language" to Locale.getDefault().toLanguageTag())
                )
            } else if (state is LoginError) {
                activity?.toast(when (state) {
                    RequiredPermissionsNotGranted -> R.string.oauth_failed_permissions
                    CommunicationError -> R.string.oauth_communication_error
                }, Toast.LENGTH_LONG)

                viewModel.resetLogin()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    /* ------------------------------------------------------------------------------------------ */

    companion object {
        fun create(launchAuth: Boolean = false): LoginFragment {
            val f = LoginFragment()
            f.arguments = bundleOf(ARG_LAUNCH_AUTH to launchAuth)
            return f
        }

        private const val ARG_LAUNCH_AUTH = "launch_auth"
    }

    private inner class OAuthWebViewClient : WebViewClient() {

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            if (!viewModel.isAuthorizationResponseUrl(url)) return false
            viewModel.finishAuthorization(url)
            return true
        }

        @Deprecated("Deprecated in Java")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, url: String?) {
            viewModel.failAuthorization(url.toString(), errorCode, description)
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            binding.progressView.visibility = View.VISIBLE
            backPressedCallback.isEnabled = view.canGoBack()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            binding.progressView.visibility = View.INVISIBLE
        }
    }
}
