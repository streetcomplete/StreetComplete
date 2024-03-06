package de.westnordost.streetcomplete.screens.user.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.AuthorizationException
import de.westnordost.streetcomplete.data.user.OAUTH2_AUTHORIZATION_URL
import de.westnordost.streetcomplete.data.user.OAUTH2_CLIENT_ID
import de.westnordost.streetcomplete.data.user.OAUTH2_REDIRECT_URI
import de.westnordost.streetcomplete.data.user.OAUTH2_REQUESTED_SCOPES
import de.westnordost.streetcomplete.data.user.OAUTH2_REQUIRED_SCOPES
import de.westnordost.streetcomplete.data.user.OAUTH2_TOKEN_URL
import de.westnordost.streetcomplete.data.user.oauth.OAuthAuthorizationParams
import de.westnordost.streetcomplete.data.user.oauth.OAuthException
import de.westnordost.streetcomplete.data.user.oauth.OAuthService
import de.westnordost.streetcomplete.data.user.oauth.extractAuthorizationCode
import de.westnordost.streetcomplete.databinding.FragmentOauthBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/** Fragment that manages the OAuth 1 authentication process in a webview*/
class OAuthFragment : Fragment(R.layout.fragment_oauth), HasTitle {

    private val binding by viewBinding(FragmentOauthBinding::bind)

    interface Listener {
        fun onOAuthSuccess(accessToken: String)
        fun onOAuthFailed(e: Exception?)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener
    private val webViewClient: OAuthWebViewClient = OAuthWebViewClient()
    private val oAuthService: OAuthService by inject()
    private lateinit var oAuth: OAuthAuthorizationParams

    override val title: String get() = getString(R.string.user_login)

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            binding.webView.goBack()
        }
    }

    /* --------------------------------------- Lifecycle --------------------------------------- */

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.webView.settings.userAgentString = ApplicationConstants.USER_AGENT
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.allowContentAccess = true
        binding.webView.settings.setSupportZoom(false)
        binding.webView.webViewClient = webViewClient
        viewLifecycleScope.launch { continueAuthentication() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        oAuth = inState?.getString(OAUTH)?.let { Json.decodeFromString(it) }
            ?: OAuthAuthorizationParams(
                OAUTH2_AUTHORIZATION_URL,
                OAUTH2_TOKEN_URL,
                OAUTH2_CLIENT_ID,
                OAUTH2_REQUESTED_SCOPES,
                OAUTH2_REDIRECT_URI
            )
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(OAUTH, Json.encodeToString(oAuth))
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.stopLoading()
    }

    /* ------------------------------------------------------------------------------------------ */

    private suspend fun continueAuthentication() {
        try {
            binding.webView.visibility = View.VISIBLE
            binding.webView.loadUrl(
                oAuth.authorizationRequestUrl,
                mutableMapOf("Accept-Language" to Locale.getDefault().toLanguageTag())
            )
            val authorizationCode = webViewClient.awaitOAuthCallback()
            binding.webView.visibility = View.INVISIBLE

            binding.progressView.visibility = View.VISIBLE
            val accessTokenResponse = withContext(Dispatchers.IO) {
                oAuthService.retrieveAccessToken(oAuth, authorizationCode)
            }
            // not all requires scopes granted
            if (accessTokenResponse.grantedScopes?.containsAll(OAUTH2_REQUIRED_SCOPES) == false) {
                activity?.toast(R.string.oauth_failed_permissions, Toast.LENGTH_LONG)
                listener?.onOAuthFailed(null)
                return
            }

            listener?.onOAuthSuccess(accessTokenResponse.accessToken)
            binding.progressView.visibility = View.INVISIBLE
        } catch (e: Exception) {
            if (e is OAuthException) {
                activity?.toast(e.description ?: e.error, Toast.LENGTH_LONG)
            } else {
                // otherwise it is some connection error the user doesn't need to know the details about
                activity?.toast(R.string.oauth_communication_error, Toast.LENGTH_LONG)
            }
            Log.e(TAG, "Error during authorization", e)
            listener?.onOAuthFailed(e)
        }
    }

    /* ---------------------------------------------------------------------------------------- */

    companion object {
        const val TAG = "OAuthDialogFragment"

        private const val OAUTH = "oauth"
    }

    private inner class OAuthWebViewClient : WebViewClient() {

        private var continuation: Continuation<String>? = null
        suspend fun awaitOAuthCallback(): String = suspendCoroutine { continuation = it }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            if (!oAuth.itsForMe(url)) return false
            continuation?.resumeWith(runCatching { extractAuthorizationCode(url) })
            return true
        }

        @Deprecated("Deprecated in Java")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, url: String?) {
            continuation?.resumeWithException(
                AuthorizationException("Error for URL " + url + if (description != null) ": $description" else "")
            )
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
