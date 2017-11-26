package de.westnordost.streetcomplete.oauth;

import android.content.SharedPreferences;

import dagger.Module;
import dagger.Provides;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

@Module
public class OAuthModule
{
	private static final String BASE_OAUTH_URL = "https://www.openstreetmap.org/oauth/";

	private static final String CONSUMER_KEY = "L3JyJMjVk6g5atwACVySRWgmnrkBAH7u0U18ALO7";
	private static final String CONSUMER_SECRET = "uNjPaXZw15CPHdCSeMzttRm20tyFGaBPO7jHt52c";

	@Provides public static OAuthPrefs oAuthPrefs(SharedPreferences prefs)
	{
		return new OAuthPrefs(prefs, OAuthModule::defaultOAuthConsumer);
	}

	@Provides public static OAuthProvider oAuthProvider()
	{
		return new DefaultOAuthProvider(
				BASE_OAUTH_URL + "request_token",
				BASE_OAUTH_URL + "access_token",
				BASE_OAUTH_URL + "authorize");
	}

	@Provides public static OAuthConsumer defaultOAuthConsumer()
	{
		return new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
	}
}
