package de.westnordost.streetcomplete.data.download;

import android.content.SharedPreferences;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestTypes;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

public class MobileDataAutoDownloadStrategy extends AActiveRadiusStrategy
{
	@Inject public MobileDataAutoDownloadStrategy(OsmQuestDao osmQuestDB,
										  DownloadedTilesDao downloadedTilesDao,
										  QuestTypes questTypes, SharedPreferences prefs)
	{
		super(osmQuestDB, downloadedTilesDao, questTypes, prefs);
	}

	@Override public int getQuestTypeDownloadCount()
	{
		return 3;
	}

	@Override protected int getMinQuestsInActiveRadiusPerKm2()
	{
		return 8;
	}

	@Override protected int[] getActiveRadii()
	{
		return new int[]{400};
	}

	@Override protected int getDownloadRadius()
	{
		return 800;
	}
}
