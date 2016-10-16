package de.westnordost.osmagent.quests;

import android.content.SharedPreferences;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmagent.OsmagentConstants;
import de.westnordost.osmagent.oauth.OAuth;
import de.westnordost.osmagent.quests.osm.download.ElementGeometryCreator;
import de.westnordost.osmagent.quests.osm.download.OverpassMapDataDao;
import de.westnordost.osmagent.quests.osm.download.OverpassMapDataParser;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.notes.NotesDao;
import oauth.signpost.OAuthConsumer;

@Module
public class OsmModule
{
	public static String OSM_API_URL = "https://api.openstreetmap.org/api/0.6/";

	public static String OVERPASS_API_URL = "http://overpass-api.de/api/";

	@Provides @Singleton public static OsmConnection osmConnection(OAuthConsumer consumer)
	{
		return new OsmConnection(OSM_API_URL, OsmagentConstants.USER_AGENT, consumer);
	}

	@Provides public static OAuthConsumer oAuthConsumer(SharedPreferences prefs)
	{
		return OAuth.loadConsumer(prefs);
	}

	@Provides public static MapDataFactory mapDataFactory()
	{
		return new OsmMapDataFactory();
	}

	@Provides public static OverpassMapDataDao overpassMapDataDao(
			Provider<OverpassMapDataParser> parserProvider)
	{
		OsmConnection overpassConnection = new OsmConnection(
				OVERPASS_API_URL, OsmagentConstants.USER_AGENT, null);
		return new OverpassMapDataDao(overpassConnection, parserProvider);
	}

	@Provides public static OverpassMapDataParser overpassMapDataParser()
	{
		return new OverpassMapDataParser(new ElementGeometryCreator(), new OsmMapDataFactory());
	}

	@Provides public static ChangesetsDao changesetsDao(OsmConnection osm)
	{
		return new ChangesetsDao(osm);
	}

	@Provides public static NotesDao notesDao(OsmConnection osm)
	{
		return new NotesDao(osm);
	}

	@Provides public static MapDataDao mapDataDao(OsmConnection osm)
	{
		return new MapDataDao(osm);
	}
}
