package de.westnordost.osmagent.data.download;

import android.content.SharedPreferences;

import javax.inject.Inject;

import de.westnordost.osmagent.data.QuestTypes;
import de.westnordost.osmagent.data.osm.persist.OsmQuestDao;
import de.westnordost.osmagent.data.tiles.DownloadedTilesDao;

public class WifiAutoDownloadStrategy extends AActiveRadiusStrategy
{
	@Inject public WifiAutoDownloadStrategy(OsmQuestDao osmQuestDB,
											DownloadedTilesDao downloadedTilesDao,
											QuestTypes questTypes, SharedPreferences prefs)
	{
		super(osmQuestDB, downloadedTilesDao, questTypes, prefs);
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
		return 3;
	}

	@Override protected int getMinQuestsInActiveRadiusPerKm2()
	{
		return 12;
	}

	@Override protected int getActiveRadius()
	{
		return 1200;
	}

	@Override protected int getDownloadRadius()
	{
		return 2400;
	}
}
