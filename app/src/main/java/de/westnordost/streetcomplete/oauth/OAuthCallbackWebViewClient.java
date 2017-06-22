package de.westnordost.streetcomplete.oauth;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import de.westnordost.streetcomplete.util.AsyncTaskListener;

/** WebViewClient that fetches the OAuth verification code from the OAuth authentication webpage */
public class OAuthCallbackWebViewClient extends WebViewClient
{
	public static final String TAG = "OAuth";

	// magic callback url for webpage to tell this dialog that the user has confirmed the authorization
	private static final String CALLBACK_URL = "streetcomplete://oauth/";

	private AsyncTaskListener<String> listener;
	private ViewGroup progressDisplay;
	private WebView webView;

	private boolean errorOccured;

	public OAuthCallbackWebViewClient(AsyncTaskListener<String> listener, WebView webView, ViewGroup progressDisplay)
	{
		this.listener = listener;
		this.progressDisplay = progressDisplay;
		this.webView = webView;
	}

    @Override
    @SuppressWarnings("deprecation")
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
    {
        onError(new OAuthException(errorCode, description, failingUrl));
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
    {
        onError(new OAuthException(request, errorResponse));
    }

    @TargetApi(23)
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
    {
        onError(new OAuthException(request, error));
    }

    @Override
    public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error)
    {
        onError(null);
    }

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon)
	{
		errorOccured = false;

		progressDisplay.setVisibility(View.VISIBLE);
		webView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onPageFinished(WebView view, String url)
	{
		if(errorOccured) return;
		if(url.startsWith(CALLBACK_URL)) return;

		progressDisplay.setVisibility(View.INVISIBLE);
		webView.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String stringUrl)
	{
		Log.i(TAG, "Loading URL " + stringUrl);
		if (!stringUrl.startsWith(CALLBACK_URL)) return false;

		try
		{
			URI url = new URI(stringUrl);
			Map<String, String> params = getQueryMap(url.getQuery());

			// also available but not needed:
			//String token = params.get("oauth_token");
			listener.onSuccess(params.get("oauth_verifier"));
		}
		catch(URISyntaxException e)
		{
			listener.onError(e);
		}

		return true;
	}

	private void onError(Exception e)
	{
		listener.onError(e);
		errorOccured = true;
	}

	private static Map<String, String> getQueryMap(String query)
	{
		if (query == null) return null;

		String[] params = query.split("&");
		Map<String, String> map = new HashMap<>();
		for (String param : params)
		{
			String[] keyValue = param.split("=");
			String name = param.split("=")[0];

			String value = null;
			if (keyValue.length > 1)
			{
				value = param.split("=")[1];
			}
			map.put(name, value);
		}
		return map;
	}
}
