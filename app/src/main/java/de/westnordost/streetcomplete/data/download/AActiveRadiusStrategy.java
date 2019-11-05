package de.westnordost.streetcomplete.data.download;

import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.data.QuestStatus;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider;
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
	private final OrderedVisibleQuestTypesProvider questTypesProvider;

	public AActiveRadiusStrategy(
			OsmQuestDao osmQuestDB, DownloadedTilesDao downloadedTilesDao,
			OrderedVisibleQuestTypesProvider questTypesProvider)
	{
		this.osmQuestDB = osmQuestDB;
		this.downloadedTilesDao = downloadedTilesDao;
		this.questTypesProvider = questTypesProvider;
	}

	private boolean mayDownloadHere(LatLon pos, int radius, List<String> questTypeNames)
	{
		BoundingBox bbox = SphericalEarthMath.enclosingBoundingBox(pos, radius);

		// nothing more to download
		Rect tiles = SlippyMapMath.enclosingTiles(bbox, ApplicationConstants.QUEST_TILE_ZOOM);
		long questExpirationTime = ApplicationConstants.REFRESH_QUESTS_AFTER;
		long ignoreOlderThan = Math.max(0,System.currentTimeMillis() - questExpirationTime);
		Set<String> alreadyDownloaded = new HashSet<>(downloadedTilesDao.get(tiles, ignoreOlderThan));
		List<String> notAlreadyDownloaded = new ArrayList<>();
		for (String questTypeName : questTypeNames)
		{
			if (!alreadyDownloaded.contains(questTypeName)) notAlreadyDownloaded.add(questTypeName);
		}

		if(notAlreadyDownloaded.isEmpty())
		{
			Log.i(TAG, "Not downloading quests because everything has been downloaded already in " + radius + "m radius");
			return false;
		}
		double areaInKm2 = SphericalEarthMath.enclosedArea(bbox) / 1000 / 1000;
		// got enough quests in vicinity
		int visibleQuests = osmQuestDB.getCount(Arrays.asList(QuestStatus.NEW), bbox, null, notAlreadyDownloaded, null);
		if(visibleQuests / areaInKm2 > getMinQuestsInActiveRadiusPerKm2())
		{
			Log.i(TAG, "Not downloading quests because there are enough quests in " + radius + "m radius");
			return false;
		}

		return true;
	}

	private List<String> getQuestTypeNames()
	{
		List<QuestType<?>> questTypes = questTypesProvider.get();
		List<String> questTypeNames = new ArrayList<>(questTypes.size());
		for (QuestType questType : questTypes)
		{
			questTypeNames.add(questType.getClass().getSimpleName());
		}
		return questTypeNames;
	}

	@Override public boolean mayDownloadHere(LatLon pos)
	{
		List<String> questTypeNames = getQuestTypeNames();
		for (int activeRadius : getActiveRadii())
		{
			if(mayDownloadHere(pos, activeRadius, questTypeNames)) return true;
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
