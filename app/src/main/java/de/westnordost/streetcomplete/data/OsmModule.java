package de.westnordost.streetcomplete.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmapi.user.UserDao;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver;
import de.westnordost.streetcomplete.data.osm.download.ElementGeometryCreator;
import de.westnordost.streetcomplete.data.osm.download.OsmApiWayGeometrySource;
import de.westnordost.streetcomplete.data.osm.download.OverpassOldMapDataDao;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestSplitWayDao;
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao;
import de.westnordost.streetcomplete.data.osm.upload.OpenQuestChangesetsManager;
import de.westnordost.streetcomplete.data.osm.upload.OsmQuestsUploader;
import de.westnordost.streetcomplete.data.osm.upload.SingleOsmElementTagChangesUpload;
import de.westnordost.streetcomplete.data.osm.upload.SplitSingleWayUpload;
import de.westnordost.streetcomplete.data.osm.upload.SplitWaysUploader;
import de.westnordost.streetcomplete.data.osm.upload.UndoOsmQuestsUploader;
import de.westnordost.streetcomplete.data.osmnotes.CreateNotesUploader;
import de.westnordost.streetcomplete.data.osmnotes.OsmAvatarsDownload;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestsChangesUploader;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.data.upload.Uploader;
import de.westnordost.streetcomplete.data.upload.VersionIsBannedChecker;
import de.westnordost.streetcomplete.oauth.OAuthPrefs;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataParser;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.MapDataFactory;
import de.westnordost.osmapi.map.OsmMapDataFactory;
import de.westnordost.osmapi.notes.NotesDao;
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsDao;
import de.westnordost.streetcomplete.util.ImageUploader;
import oauth.signpost.OAuthConsumer;

@Module
public class OsmModule
{
	public static final String
		OSM_API_URL = "https://api.openstreetmap.org/api/0.6/",
		OVERPASS_API_WITH_ATTIC_DATA_URL = "https://lz4.overpass-api.de/api/", // required for some tests
		ONEWAY_API_URL = "https://www.westnordost.de/streetcomplete/oneway-data-api/";


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

	// see https://wiki.openstreetmap.org/wiki/Overpass_API/Overpass_QL#timeout:
	// default value is 180 seconds
	// give additional 4 seconds to get and process refusal from Overpass
	// or maybe a bit late response rather than trigger timeout exception
	private static int OVERPASS_QUERY_TIMEOUT_IN_MILISECONDS = (180 + 4) * 1000;

	@Provides public static OverpassMapDataDao overpassMapDataDao(
			Provider<OverpassMapDataParser> parserProvider, SharedPreferences prefs)
	{
		Integer timeout = OVERPASS_QUERY_TIMEOUT_IN_MILISECONDS;
		OsmConnection overpassConnection = new OsmConnection(
			prefs.getString(Prefs.OVERPASS_URL, OVERPASS_API_WITH_ATTIC_DATA_URL), ApplicationConstants.USER_AGENT, null, timeout);
		return new OverpassMapDataDao(overpassConnection, parserProvider);
	}

	@Provides public static OverpassOldMapDataDao overpassOldMapDataDao(
		Provider<OverpassMapDataParser> parserProvider, String date)
	{
		Integer timeout = OVERPASS_QUERY_TIMEOUT_IN_MILISECONDS;
		OsmConnection overpassConnection = new OsmConnection(
			OVERPASS_API_WITH_ATTIC_DATA_URL, ApplicationConstants.USER_AGENT, null, timeout);
		return new OverpassOldMapDataDao(overpassConnection, parserProvider, date);
	}

	@Provides public static TrafficFlowSegmentsDao trafficFlowSegmentsDao()
	{
		return new TrafficFlowSegmentsDao(ONEWAY_API_URL);
	}

	@Provides public static OverpassMapDataParser overpassMapDataParser()
	{
		return new OverpassMapDataParser(new OsmMapDataFactory());
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

	@Provides public static OsmQuestsUploader osmQuestsUpload(
		MergedElementDao elementDB, ElementGeometryDao elementGeometryDB,
		OpenQuestChangesetsManager changesetManager, OsmQuestGiver questGiver,
		QuestStatisticsDao statisticsDB, OsmApiWayGeometrySource wayGeometrySource,
		OsmQuestDao questDB, SingleOsmElementTagChangesUpload singleChangeUpload,
		DownloadedTilesDao downloadedTilesDao) {
		return new OsmQuestsUploader(elementDB, elementGeometryDB, changesetManager, questGiver,
			statisticsDB, new ElementGeometryCreator(wayGeometrySource), questDB,
			singleChangeUpload, downloadedTilesDao);
	}

	@Provides public static UndoOsmQuestsUploader undoOsmQuestsUpload(
		MergedElementDao elementDB, ElementGeometryDao elementGeometryDB,
		OpenQuestChangesetsManager changesetManager, OsmQuestGiver questGiver,
		QuestStatisticsDao statisticsDB, OsmApiWayGeometrySource wayGeometrySource,
		UndoOsmQuestDao questDB, SingleOsmElementTagChangesUpload singleChangeUpload) {
		return new UndoOsmQuestsUploader(elementDB, elementGeometryDB, changesetManager, questGiver,
			statisticsDB, new ElementGeometryCreator(wayGeometrySource), questDB, singleChangeUpload);
	}

	@Provides public static SplitWaysUploader splitWaysUpload(
		MergedElementDao elementDB, ElementGeometryDao elementGeometryDB,
		OpenQuestChangesetsManager changesetManager, OsmQuestGiver questGiver,
		QuestStatisticsDao statisticsDB, OsmApiWayGeometrySource wayGeometrySource,
		OsmQuestSplitWayDao questDB, SplitSingleWayUpload singleUpload) {
		return new SplitWaysUploader(elementDB, elementGeometryDB, changesetManager, questGiver,
			statisticsDB, new ElementGeometryCreator(wayGeometrySource), questDB, singleUpload);
	}

	@Provides public static OsmAvatarsDownload avatarsDownload(UserDao userDao, Context context)
	{
		return new OsmAvatarsDownload(userDao, getAvatarsCacheDirectory(context));
	}

	@Provides public static ImageUploader imageUploader()
	{
		return new ImageUploader(ApplicationConstants.SC_PHOTO_SERVICE_URL);
	}

	@Provides public static List<? extends Uploader> uploaders(
		OsmNoteQuestsChangesUploader osmNoteQuestsChangesUploader,
		UndoOsmQuestsUploader undoOsmQuestsUploader, OsmQuestsUploader osmQuestsUploader,
		SplitWaysUploader splitWaysUploader, CreateNotesUploader createNotesUploader
	) {
		return Arrays.asList(osmNoteQuestsChangesUploader, undoOsmQuestsUploader, osmQuestsUploader,
			splitWaysUploader, createNotesUploader);
	}

	public static File getAvatarsCacheDirectory(Context context)
	{
		return new File(context.getCacheDir(), ApplicationConstants.AVATARS_CACHE_DIRECTORY);
	}

	@Provides public static VersionIsBannedChecker checkVersionIsBanned() {
		return new VersionIsBannedChecker(
			"https://www.westnordost.de/streetcomplete/banned_versions.txt",
			ApplicationConstants.USER_AGENT);
	}
}
