package de.westnordost.streetcomplete.data.download;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.Log;


import de.westnordost.streetcomplete.ApplicationConstants;
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

	public boolean mayDownloadHere(LatLon pos, int radius)
	{
		BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(pos, radius);

		double areaInKm2 = SphericalEarthMath.enclosedArea(bbox) / 1000 / 1000;

		// got enough quests in vicinity
		int visibleQuests = osmQuestDB.getCount(bbox, QuestStatus.NEW);
		if(visibleQuests / areaInKm2 > getMinQuestsInActiveRadiusPerKm2())
		{
			Log.i(TAG, "Not downloading quests because there are enough quests in " + radius + "m radius");
			return false;
		}

		// (this check is more computational effort, so its done after the vicinity check)
		// nothing more to download
		Rect tiles = SlippyMapMath.enclosingTiles(bbox, ApplicationConstants.QUEST_TILE_ZOOM);
		long questExpirationTime = ApplicationConstants.REFRESH_QUESTS_AFTER;
		long ignoreOlderThan = Math.max(0,System.currentTimeMillis() - questExpirationTime);
		int alreadyDownloadedQuestTypes = downloadedTilesDao.get(tiles, ignoreOlderThan).size();
		if(alreadyDownloadedQuestTypes >= questTypes.getAmount())
		{
			Log.i(TAG, "Not downloading quests because everything has been downloaded already in" + radius + "m radius");
			return false;
		}

		return true;
	}

	@Override public boolean mayDownloadHere(LatLon pos)
	{
		for (int activeRadius : getActiveRadii())
		{
			if(mayDownloadHere(pos, activeRadius)) return true;
		}
		return false;
	}

	@Override public BoundingBox getDownloadBoundingBox(LatLon pos)
	{
		return SphericalEarthMath.enclosingBoundingBox(pos, getDownloadRadius());
	}

	protected abstract int getMinQuestsInActiveRadiusPerKm2();
	protected abstract int[] getActiveRadii();
	protected abstract int getDownloadRadius();

}
