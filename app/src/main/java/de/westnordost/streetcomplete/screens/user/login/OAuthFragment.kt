package de.westnordost.streetcomplete.screens.user.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.AuthorizationException
import de.westnordost.streetcomplete.data.user.CALLBACK_HOST
import de.westnordost.streetcomplete.data.user.CALLBACK_SCHEME
import de.westnordost.streetcomplete.databinding.FragmentOauthBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.Version
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
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
    private val callbackUrl get() = "$CALLBACK_SCHEME://$CALLBACK_HOST"
    private val webViewClient: OAuthWebViewClient = OAuthWebViewClient()

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

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.stopLoading()
    }

    /* ------------------------------------------------------------------------------------------ */

    private suspend fun continueAuthentication() {
        try {
            binding.webView.loadUrl(
                createAuthorizeUrl,
                mutableMapOf("Accept-Language" to Locale.getDefault().toLanguageTag())
            )
            val authorizationCode = webViewClient.awaitOAuthCallback()
            binding.progressView.visibility = View.VISIBLE
            val accessToken = withContext(Dispatchers.IO) {
                retrieveAccessToken(authorizationCode)
            }
            listener?.onOAuthSuccess(accessToken)
            binding.progressView.visibility = View.INVISIBLE
        } catch (e: Exception) {
            activity?.toast(R.string.oauth_communication_error, Toast.LENGTH_LONG)
            Log.e(TAG, "Error during authorization", e)
            listener?.onOAuthFailed(e)
        }
    }

    /* ---------------------------------------------------------------------------------------- */

    companion object {
        const val TAG = "OAuthDialogFragment"
    }

    private inner class OAuthWebViewClient : WebViewClient() {

        private var continuation: Continuation<String>? = null
        suspend fun awaitOAuthCallback(): String = suspendCoroutine { continuation = it }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val uri = url?.toUri() ?: return false
            if (!uri.isHierarchical) return false
            if (uri.scheme != CALLBACK_SCHEME || uri.host != CALLBACK_HOST) return false
            val authorizationCode = uri.getQueryParameter("code")
            if (authorizationCode != null) {
                continuation?.resume(authorizationCode)
            } else {
                continuation?.resumeWithException(
                    AuthorizationException("oauth_verifier parameter not set by provider")
                )
            }
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
