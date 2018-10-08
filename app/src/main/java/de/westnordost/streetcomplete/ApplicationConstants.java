package de.westnordost.streetcomplete;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ApplicationConstants
{
	public final static String
		NAME = "StreetComplete",
		USER_AGENT = NAME + " " + BuildConfig.VERSION_NAME,
		QUESTTYPE_TAG_KEY = NAME + ":quest_type";

	// date of birth of StreetComplete ;-) (first Google Play version)
	public final static Date DATE_OF_BIRTH = new GregorianCalendar(2017,Calendar.FEBRUARY,20).getTime();

	public final static double
		MAX_DOWNLOADABLE_AREA_IN_SQKM = 50,
		MIN_DOWNLOADABLE_AREA_IN_SQKM = 4;

	public final static double MIN_DOWNLOADABLE_RADIUS_IN_METERS = 1000;

	public final static int QUEST_TILE_ZOOM = 14;

	public final static int NOTE_MIN_ZOOM = 15;

	/** How many quests to download when pressing manually on "download quests" */
	public final static int MANUAL_DOWNLOAD_QUEST_TYPE_COUNT = 10;

	/** a "best before" duration for quests. Quests will not be downloaded again for any tile
	 *  before the time expired */
	public static final int REFRESH_QUESTS_AFTER = 7*24*60*60*1000; // one week in ms

	public static final String AVATARS_CACHE_DIRECTORY = "osm_user_avatars";

	public static final String SC_PHOTO_SERVICE_URL = "https://westnordost.de/streetcomplete/photo-upload/"; // must have trailing /

	public static final int ATTACH_PHOTO_QUALITY = 60;
	public static final int ATTACH_PHOTO_MAXWIDTH = 1280; // WXGA

	public static final String NOTIFICATIONS_CHANNEL_DOWNLOAD = "downloading";
}
