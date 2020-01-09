package de.westnordost.streetcomplete.oauth

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.fragment.app.DialogFragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toast
import kotlinx.coroutines.*
import oauth.signpost.OAuthConsumer
import oauth.signpost.OAuthProvider
import javax.inject.Inject
import javax.inject.Provider

// TODO generify (not specific to OSM)

class OsmOAuthDialogFragment
	: DialogFragment(), CoroutineScope by CoroutineScope(Dispatchers.Main)
{
	@Inject internal lateinit var consumerProvider: Provider<OAuthConsumer>
	@Inject internal lateinit var provider: OAuthProvider

	interface Listener {
		fun onOAuthSuccess(consumer: OAuthConsumer)
		fun onOAuthFailed(e: Exception?)
	}

    private var consumer: OAuthConsumer? = null
    private var listener: Listener? = null
    private var verifier: String? = null

    private var state: State = State.INITIAL
    private enum class State {
        INITIAL,
		RETRIEVING_REQUEST_TOKEN,
		AUTHENTICATING_IN_BROWSER,
		AUTHENTICATED_FROM_BROWSER,
		RETRIEVING_ACCESS_TOKEN,
		AUTHORIZED,
		CANCELLED
    }

	init {
		Injector.instance.applicationComponent.inject(this)
	}

	/* --------------------------------------- Lifecycle --------------------------------------- */

    override fun onCreate(inState: Bundle?) {
        super.onCreate(inState)
        if (inState != null) {
            consumer = inState.getSerializable(CONSUMER) as OAuthConsumer
            state = State.valueOf(inState.getString(STATE)!!)
        } else {
            consumer = consumerProvider.get()
            state = State.INITIAL
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_oauth, container, false)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as Listener
    }

    override fun onResume() {
        super.onResume()
        continueAuthentication()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.toast(R.string.oauth_cancelled, Toast.LENGTH_SHORT)
        onAuthorizationFailed(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(CONSUMER, consumer)
        outState.putString(STATE, state.toString())
        super.onSaveInstanceState(outState)
    }

	override fun onDestroy() {
		super.onDestroy()
		coroutineContext.cancel()
	}

    /* ------------------------------------------------------------------------------------------ */

	fun onNewIntent(intent: Intent) {
		val uri = intent.data
		if (uri != null && uri.scheme == CALLBACK_SCHEME && uri.host == CALLBACK_HOST) {
			verifier = uri.getQueryParameter("oauth_verifier")
			state = State.AUTHENTICATED_FROM_BROWSER
		} else {
			verifier = null
		}
	}

    private fun continueAuthentication() {
        if (state == State.INITIAL) {
            state = State.RETRIEVING_REQUEST_TOKEN
            launch { retrieveRequestToken()  }
        } else if (state == State.AUTHENTICATED_FROM_BROWSER) {
            state = State.RETRIEVING_ACCESS_TOKEN
            launch { retrieveAccessToken() }
        }
    }

	private suspend fun retrieveRequestToken() {
		try {
			val authorizeUrl = withContext(Dispatchers.IO) {
				provider.retrieveRequestToken(consumer, CALLBACK_URL)
			}
			if (state == State.CANCELLED) return
			val activity = activity ?: return
			val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizeUrl))
			intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity.packageName)
			intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_SINGLE_TOP
			startActivity(intent)

			state = State.AUTHENTICATING_IN_BROWSER

		} catch (e: Exception) {
			onAuthorizationError(e)
		}
	}

	private suspend fun retrieveAccessToken() {
		try {
			withContext(Dispatchers.IO) {
				provider.retrieveAccessToken(consumer, verifier)
			}
			if (state == State.CANCELLED) return

			onAuthorizationSuccess()

		} catch (e: Exception) {
			onAuthorizationError(e)
		}
	}

    @UiThread private fun onAuthorizationError(e: Exception) {
		activity?.toast(R.string.oauth_communication_error, Toast.LENGTH_LONG)

        Log.e(TAG, "Error during authorization", e)
        onAuthorizationFailed(e)
    }

    @UiThread private fun onAuthorizationSuccess() {
        state = State.AUTHORIZED
		val consumer = consumer
		if (consumer != null) {
			listener?.onOAuthSuccess(consumer)
		} else {
			listener?.onOAuthFailed(null)
		}
        dismiss()
    }

    @UiThread private fun onAuthorizationFailed(e: Exception?) {
        state = State.CANCELLED
		listener?.onOAuthFailed(e)
        dismiss()
    }

    /* ---------------------------------------------------------------------------------------- */

    companion object {
        const val TAG = "OsmOAuthDialogFragment"

        // for loading and saving from bundle
        private const val CONSUMER = "consumer"
        private const val STATE = "state"

        private const val CALLBACK_SCHEME = "streetcomplete"
        private const val CALLBACK_HOST = "oauth"
        private const val CALLBACK_URL = "$CALLBACK_SCHEME://$CALLBACK_HOST"
    }
}
