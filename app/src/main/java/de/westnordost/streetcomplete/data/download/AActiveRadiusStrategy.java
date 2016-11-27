package de.westnordost.streetcomplete.data.download;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.Log;


import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestTypes;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.util.SlippyMapMath;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

/** Quest auto download strategy that observes that a minimum amount of quests in a predefined
 *  radius around the user is not undercut */
public abstract class AActiveRadiusStrategy implements QuestAutoDownloadStrategy
{
	private static final String TAG = "AutoQuestDownload";

	private final OsmQuestDao osmQuestDB;
	private final DownloadedTilesDao downloadedTilesDao;
	private final QuestTypes questTypes;
	private final SharedPreferences prefs;

	public AActiveRadiusStrategy(
			OsmQuestDao osmQuestDB, DownloadedTilesDao downloadedTilesDao, QuestTypes questTypes,
			SharedPreferences prefs)
	{
		this.osmQuestDB = osmQuestDB;
		this.downloadedTilesDao = downloadedTilesDao;
		this.questTypes = questTypes;
		this.prefs = prefs;
	}

	@Override public boolean mayDownloadHere(LatLon pos)
	{
		BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(pos, getActiveRadius());

		double areaInKm2 = SphericalEarthMath.enclosedArea(bbox) / 1000 / 1000;

		// got enough quests in vicinity
		int visibleQuests = osmQuestDB.getCount(bbox, QuestStatus.NEW);
		if(visibleQuests / areaInKm2 > getMinQuestsInActiveRadiusPerKm2())
		{
			Log.i(TAG, "Not downloading quests because there are enough quests around here");
			return false;
		}

		// (this check is more computational effort, so its done after the vicinity check)
		// nothing more to download
		int totalQuestTypes = questTypes.getAmount() + 1; // +1 because of note quests
		Rect tiles = SlippyMapMath.enclosingTiles(bbox, ApplicationConstants.QUEST_TILE_ZOOM);
		long questExpirationTime = Integer.parseInt(prefs.getString(Prefs.QUESTS_EXPIRATION_TIME_IN_MIN, "0")) * 1000 * 60;
		long ignoreOlderThan = Math.max(0,System.currentTimeMillis() - questExpirationTime);
		int alreadyDownloadedQuestTypes = downloadedTilesDao.getQuestTypeNames(tiles, ignoreOlderThan).size();
		if(alreadyDownloadedQuestTypes >= totalQuestTypes)
		{
			Log.i(TAG, "Not downloading quests because everything has been downloaded here already");
			return false;
		}

		return true;
	}

	@Override public BoundingBox getDownloadBoundingBox(LatLon pos)
	{
		return SphericalEarthMath.enclosingBoundingBox(pos, getDownloadRadius());
	}

	protected abstract int getMinQuestsInActiveRadiusPerKm2();
	protected abstract int getActiveRadius();
	protected abstract int getDownloadRadius();

}
