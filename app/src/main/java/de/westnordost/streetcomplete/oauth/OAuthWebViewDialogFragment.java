package de.westnordost.streetcomplete.oauth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;

import java.util.List;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.user.PermissionsDao;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.OsmModule;
import de.westnordost.streetcomplete.util.AsyncTaskListener;
import de.westnordost.streetcomplete.util.InlineAsyncTask;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthException;

/** Dialog used to authorize the application with OAuth 1.0a. Create this dialog via
 *  OAuthWebViewDialogFragment.create and provide an OAuthConsumer and OAuthProvider.
 *
 *	The calling class must implement the interface OAuthWebViewDialogFragment.OAuthListener with
 *  which the class is notified when the process is complete. Check getToken() and getTokenSecret()
 *  of the parameter passed to onOAuthAuthorized to retrieve the access token and secret, the result
 *  of the whole authorization process.
 */
public class OAuthWebViewDialogFragment extends DialogFragment
{
	public static final String TAG = "OAuth";

	// magic callback url for webpage to tell this dialog that the user has confirmed the authorization
	private static final String CALLBACK_URL = "streetcomplete://oauth/";

	// for loading and saving from bundle
	private static final String
			CONSUMER = "consumer",
			PROVIDER = "provider",
			AUTHORIZE_URL = "authorizeURL",
			VERIFICATION_CODE = "verificationCode";

	// OAuth stuff
	private OAuthConsumer consumer;
	private OAuthProvider provider;
	private String authorizeURL;
	private String verificationCode;

	// for reporting back with the result
	private OAuthListener callbackListener;

	// UI components
	private WebView webView;
	private ViewGroup progressGroup;
	private ViewGroup errorGroup;

	/** To be implemented by the user of this dialog fragment. Used to report back with the result */
	public interface OAuthListener
	{
		/** Called when the authorization process has successfully finished.
		 *  @param consumer The consumer has the correct access token and secret set.
		 *  @param permissions The permissions granted to the consumer. See constants in
		 *                     de.westnordost.osmapi.user.Permission */
		void onOAuthAuthorized(OAuthConsumer consumer, List<String> permissions);

		/** Called when the authorization process failed because the user closed the dialog */
		void onOAuthCancelled();
	}

	public static OAuthWebViewDialogFragment create(@NonNull OAuthConsumer consumer,
													@NonNull OAuthProvider provider)
	{
		OAuthWebViewDialogFragment f = new OAuthWebViewDialogFragment();

		Bundle args = new Bundle();
		args.putSerializable(CONSUMER, consumer);
		args.putSerializable(PROVIDER, provider);
		f.setArguments(args);

		return f;
	}

	/* --------- The below lifecycle methods are ordered in the order they are called ----------- */

	@Override public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			callbackListener = (OAuthListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OAuthListener");
		}
	}

	@Override public void onCreate(Bundle inState)
	{
		super.onCreate(inState);

		// restore...
		if(inState != null)
		{
			consumer = (OAuthConsumer) inState.getSerializable(CONSUMER);
			provider = (OAuthProvider) inState.getSerializable(PROVIDER);
			authorizeURL = inState.getString(AUTHORIZE_URL);
			verificationCode = inState.getString(VERIFICATION_CODE);
		}
		// or initialize...
		else
		{
			consumer = (OAuthConsumer) getArguments().getSerializable(CONSUMER);
			provider = (OAuthProvider) getArguments().getSerializable(PROVIDER);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{

		View view = inflater.inflate(R.layout.oauth_web_view_dialog_fragment, container, false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		progressGroup = (ViewGroup) view.findViewById(R.id.progress);
		errorGroup = (ViewGroup) view.findViewById(R.id.error);

		webView = (WebView) view.findViewById(R.id.webview);
		// Javascript is necessary to display OpenStreetMap's OAuth page correctly
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient( new OAuthCallbackWebViewClient(
				new RetrieveVerificationCodeListener(), webView, progressGroup));

		Button tryAgain = (Button) view.findViewById(R.id.retry_button);
		tryAgain.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				restartAuthentication();
			}
		});

		return view;
	}

	@Override public void onStart()
	{
		super.onStart();
		continueAuthentication();
	}

	@Override public void onStop()
	{
		super.onStop();
		webView.stopLoading();
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState)
	{
		outState.putSerializable(CONSUMER, consumer);
		outState.putSerializable(PROVIDER, provider);
		outState.putString(AUTHORIZE_URL, authorizeURL);
		outState.putString(VERIFICATION_CODE, verificationCode);
		super.onSaveInstanceState(outState);
	}

	@Override public void onDestroyView()
	{
		super.onDestroyView();
		webView.destroy();
		webView = null;
	}

	@Override public void onCancel(DialogInterface dialog)
	{
		super.onCancel(dialog);
		if(verificationCode == null)
		{
			callbackListener.onOAuthCancelled();
		}
	}

	/* ------------------------------------------------------------------------------------------ */

	private void restartAuthentication()
	{
		authorizeURL = null;
		verificationCode = null;
		continueAuthentication();
	}

	private void continueAuthentication()
	{
		if(webView == null) return;

		progressGroup.setVisibility(View.VISIBLE);
		errorGroup.setVisibility(View.INVISIBLE);
		webView.setVisibility(View.INVISIBLE);

		if(authorizeURL == null)
		{
			Log.i(TAG, "Step 1: Retrieving request token...");
			new RetrieveRequestTokenTask().execute();
		}
		else if(verificationCode == null)
		{
			Log.i(TAG, "Step 2: Authorize app on web page...");
			if(!authorizeURL.equals(webView.getOriginalUrl()))
			{
				webView.loadUrl(authorizeURL);
			}
			else
			{
				progressGroup.setVisibility(View.INVISIBLE);
				webView.setVisibility(View.VISIBLE);
			}
		}
		else
		{
			Log.i(TAG, "Step 3: Retrieving access token and getting permissions...");
			new RetrieveAccessTokenTask().execute();
		}
	}

	private void finishAuthentication(List<String> permissions)
	{
		callbackListener.onOAuthAuthorized(consumer, permissions);
		dismiss();
	}

	private void onAuthorizationError(Exception e)
	{
		progressGroup.setVisibility(View.INVISIBLE);
		errorGroup.setVisibility(View.VISIBLE);
		if(webView != null) webView.setVisibility(View.INVISIBLE);

		Log.e(TAG, "Error during authorization", e);
	}

	/* ------------------------------------------------------------------------------------------ */

	/** Retrieves the request token asynchronously and returns the url to the website the user has
	 *  to authorize this application. */
	private class RetrieveRequestTokenTask extends InlineAsyncTask<String>
	{
		@Override protected String doInBackground() throws OAuthException
		{
			return provider.retrieveRequestToken(consumer, CALLBACK_URL);
		}

		@Override public void onSuccess(String result)
		{
			authorizeURL = result;
			continueAuthentication();
		}

		@Override public void onError(Exception e)
		{
			onAuthorizationError(e);
		}
	}

	private class RetrieveVerificationCodeListener implements AsyncTaskListener<String>
	{
		@Override  public void onSuccess(String verifier)
		{
			verificationCode = verifier;
			continueAuthentication();
		}

		@Override public void onError(Exception e)
		{
			onAuthorizationError(e);
		}
	}

	/** Retrieves the access token asynchronously */
	private class RetrieveAccessTokenTask extends InlineAsyncTask<List<String>>
	{
		@Override protected List<String> doInBackground() throws OAuthException
		{
			provider.retrieveAccessToken(consumer, verificationCode);
			// must use an own connection here and not the normal singleton because since the
			// authorization process is not finished, the new authorized consumer is not applied yet
			OsmConnection osm = OsmModule.osmConnection(consumer);
			return new PermissionsDao(osm).get();
		}

		@Override public void onSuccess(List<String> result)
		{
			if(getActivity() != null) finishAuthentication(result);
		}

		@Override public void onError(Exception e)
		{
			if(getActivity() != null) onAuthorizationError(e);
		}
	}
}