package de.westnordost.streetcomplete.oauth;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.streetcomplete.Prefs;
import oauth.signpost.OAuthConsumer;

/** Manages saving and loading OAuthConsumer persistently */
public class OAuthPrefs
{
	private final SharedPreferences prefs;
	private final Provider<OAuthConsumer> oAuthConsumerProvider;

	@Inject public OAuthPrefs(SharedPreferences prefs, Provider<OAuthConsumer> oAuthConsumerProvider)
	{
		this.prefs = prefs;
		this.oAuthConsumerProvider = oAuthConsumerProvider;
	}

	public OAuthConsumer loadConsumer()
	{
		OAuthConsumer result = oAuthConsumerProvider.get();

		String accessToken = prefs.getString(Prefs.OAUTH_ACCESS_TOKEN, null);
		String accessTokenSecret = prefs.getString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, null);

		result.setTokenWithSecret(accessToken, accessTokenSecret);

		return result;
	}

	public void saveConsumer(OAuthConsumer consumer)
	{
		SharedPreferences.Editor editor = prefs.edit();

		if(consumer != null)
		{
			editor.putString(Prefs.OAUTH_ACCESS_TOKEN, consumer.getToken());
			editor.putString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, consumer.getTokenSecret());
		}
		else
		{
			editor.remove(Prefs.OAUTH_ACCESS_TOKEN);
			editor.remove(Prefs.OAUTH_ACCESS_TOKEN_SECRET);
		}

		editor.apply();
	}

	public boolean isAuthorized()
	{
		return prefs.getString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, null) != null;
	}
}
