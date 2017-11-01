package de.westnordost.streetcomplete.data;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmapi.user.UserDao;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.oauth.OAuthPrefs;
import de.westnordost.streetcomplete.data.osm.download.ElementGeometryCreator;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataParser;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.notes.NotesDao;
import de.westnordost.streetcomplete.util.ImageUploader;
import de.westnordost.streetcomplete.util.LutimImageUploader;
import oauth.signpost.OAuthConsumer;

@Module
public class OsmModule
{
	public static String OSM_API_URL = "https://api.openstreetmap.org/api/0.6/";

	public static String OVERPASS_API_URL = "http://overpass-api.de/api/";

	/** Returns the osm connection singleton used for all daos with the saved oauth consumer */
	@Provides @Singleton public static OsmConnection osmConnection(OAuthPrefs oAuth)
	{
		return osmConnection(oAuth.loadConsumer());
	}

	/** Returns an osm connection with the supplied consumer (note the difference to the above function) */
	public static OsmConnection osmConnection(OAuthConsumer consumer)
	{
		return new OsmConnection(OSM_API_URL, ApplicationConstants.USER_AGENT, consumer);
	}

	@Provides public static MapDataFactory mapDataFactory()
	{
		return new OsmMapDataFactory();
	}

	@Provides public static OverpassMapDataDao overpassMapDataDao(
			Provider<OverpassMapDataParser> parserProvider)
	{
		OsmConnection overpassConnection = new OsmConnection(
				OVERPASS_API_URL, ApplicationConstants.USER_AGENT, null);
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

	@Provides public static UserDao userDao(OsmConnection osm)
	{
		return new UserDao(osm);
	}

	@Provides public static NotesDao notesDao(OsmConnection osm)
	{
		return new NotesDao(osm);
	}

	@Provides public static MapDataDao mapDataDao(OsmConnection osm)
	{
		return new MapDataDao(osm);
	}

	@Provides public static ImageUploader imageUploader()
	{
		LutimImageUploader imageUploader = new LutimImageUploader(ApplicationConstants.LUTIM_INSTANCE);
		imageUploader.setDeleteAfterDays(ApplicationConstants.LUTIM_DELETE_AFTER_DAYS);
		return imageUploader;
	}
}
