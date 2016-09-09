package de.westnordost.osmagent.quests;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmagent.oauth.OAuth;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import oauth.signpost.OAuthConsumer;

@Module
public class OsmModule
{
	public static String OSM_API_URL = "https://api.openstreetmap.org/api/0.6/";
	public static String USER_AGENT = "osmagent 1.0";

	@Provides @Singleton public static OsmConnection osmConnection(OAuthConsumer consumer)
	{
		return new OsmConnection(OSM_API_URL, USER_AGENT, consumer);
	}

	@Provides public static OAuthConsumer oAuthConsumer(SharedPreferences prefs)
	{
		return OAuth.loadConsumer(prefs);
	}

	@Provides public static MapDataFactory mapDataFactory()
	{
		return new OsmMapDataFactory();
	}
}
