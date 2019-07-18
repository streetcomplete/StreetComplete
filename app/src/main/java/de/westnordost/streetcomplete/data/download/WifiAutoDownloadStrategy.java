package de.westnordost.streetcomplete.data.download;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

public class WifiAutoDownloadStrategy extends AActiveRadiusStrategy
{
	@Inject public WifiAutoDownloadStrategy(
			OsmQuestDao osmQuestDB, DownloadedTilesDao downloadedTilesDao,
			Provider<List<QuestType>> questTypes)
	{
		super(osmQuestDB, downloadedTilesDao, questTypes);
	}

	/** Let's assume that if the user is on wifi, he is either at home, at work, in the hotel, at a
	 *  café,... in any case, somewhere that would act as a "base" from which he can go on an
	 *  excursion. Let's make sure he can, even if there is no or bad internet.
	 *
	 *  Since download size is almost unlimited, we can be very generous here.
	 *  However, Overpass is as limited as always, so the number of quest types we download is
	 *  limited as before */

	@Override public int getQuestTypeDownloadCount()
	{
		return 8;
	}

	@Override protected int getMinQuestsInActiveRadiusPerKm2()
	{
		return 12;
	}

	@Override protected int[] getActiveRadii()
	{
		// checks if either in 600 or 300m radius, there are enough quests.
		return new int[]{600, 300};
	}

	@Override protected int getDownloadRadius()
	{
		return 1200;
	}

	@Override
	protected int getMinQuestsTypesInActiveRadius() {
		return 3;
	}
}
