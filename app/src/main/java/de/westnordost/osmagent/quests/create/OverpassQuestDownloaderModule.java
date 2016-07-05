package de.westnordost.osmagent.quests.create;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmapi.OsmConnection;

@Module public class OverpassQuestDownloaderModule
{
	private static final String USER_AGENT = "osmagent 1.0";
	private static final String OVERPASS_API_URL = "http://overpass-api.de/api/";

	@Provides public static OverpassMapDataDao provideOverpassMapDataDao()
	{
		OsmConnection connection = new OsmConnection(OVERPASS_API_URL, USER_AGENT, null);
		return new OverpassMapDataDao(connection);
	}

}
