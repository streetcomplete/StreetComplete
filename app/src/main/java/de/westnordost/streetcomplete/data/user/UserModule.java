package de.westnordost.streetcomplete.data.user;

import android.content.Context;
import android.content.SharedPreferences;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmapi.user.UserDao;
import de.westnordost.streetcomplete.data.OsmModule;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

@Module public class UserModule
{
	public static final String STATISTICS_BACKEND_URL = ":-D";

	private static final String BASE_OAUTH_URL = "https://www.openstreetmap.org/oauth/";

	private static final String CONSUMER_KEY = "L3JyJMjVk6g5atwACVySRWgmnrkBAH7u0U18ALO7";
	private static final String CONSUMER_SECRET = "uNjPaXZw15CPHdCSeMzttRm20tyFGaBPO7jHt52c";

	@Provides public static StatisticsDownloader statisticsDownloader()
	{
		return new StatisticsDownloader(STATISTICS_BACKEND_URL);
	}

	@Provides public static OAuthStore oAuthStore(SharedPreferences prefs)
	{
		return new OAuthStore(prefs, UserModule::defaultOAuthConsumer);
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

	@Provides public static UserController userController(
		SharedPreferences prefs, UserDao userDao, UserStore userStore, Context context,
		StatisticsDownloader statisticsDownloader
	)
	{
		return new UserController(userDao, userStore, OsmModule.getAvatarsCacheDirectory(context), statisticsDownloader);
	}
}
