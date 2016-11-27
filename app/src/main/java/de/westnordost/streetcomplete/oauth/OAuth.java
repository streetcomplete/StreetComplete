package de.westnordost.streetcomplete.oauth;

import android.content.SharedPreferences;

import de.westnordost.streetcomplete.Prefs;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

public class OAuth
{
	public static final String TAG = "OAuth";

	private static final String CONSUMER_KEY = "tiwYvl7EVGjhKGDEn5rwugZTNNRihVWtmsTDr1zK";
	private static final String CONSUMER_SECRET = "Fs0bCVm3U68YNJkv8m58vu75e3XDUKQBhdnrUYOY";

	private static final String BASE_OAUTH_URL = "https://www.openstreetmap.org/oauth/";

	public static OAuthProvider createProvider()
	{
		return new DefaultOAuthProvider(
			BASE_OAUTH_URL + "request_token",
			BASE_OAUTH_URL + "access_token",
			BASE_OAUTH_URL + "authorize");
	}

	public static OAuthConsumer createConsumer()
	{
		return new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
	}

	public static OAuthConsumer loadConsumer(SharedPreferences prefs)
	{
		OAuthConsumer result = createConsumer();

		String accessToken = prefs.getString(Prefs.OAUTH_ACCESS_TOKEN, null);
		String accessTokenSecret = prefs.getString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, null);

		result.setTokenWithSecret(accessToken, accessTokenSecret);

		return result;
	}

	public static void saveConsumer(SharedPreferences prefs, OAuthConsumer consumer)
	{
		SharedPreferences.Editor editor = prefs.edit();

		editor.putString(Prefs.OAUTH_ACCESS_TOKEN, consumer.getToken());
		editor.putString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, consumer.getTokenSecret());

		editor.apply();
	}

	public static void deleteConsumer(SharedPreferences prefs)
	{
		SharedPreferences.Editor editor = prefs.edit();

		editor.remove(Prefs.OAUTH_ACCESS_TOKEN);
		editor.remove(Prefs.OAUTH_ACCESS_TOKEN_SECRET);

		editor.apply();
	}

	public static boolean isAuthorized(SharedPreferences prefs)
	{
		return prefs.getString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, null) != null;
	}
}
