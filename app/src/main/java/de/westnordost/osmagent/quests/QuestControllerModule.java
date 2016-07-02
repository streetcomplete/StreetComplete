package de.westnordost.osmagent.quests;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.overpass.OverpassMapDataDao;

@Module
public class QuestControllerModule
{
	private static final String USER_AGENT = "osmagent 1.0";
	private static final String OVERPASS_API_URL = "http://overpass-api.de/api/";

	@Provides
	public static OverpassMapDataDao provideOverpassMapDataDao()
	{
		OsmConnection connection = new OsmConnection(OVERPASS_API_URL, USER_AGENT, null);
		return new OverpassMapDataDao(connection);
	}
}
