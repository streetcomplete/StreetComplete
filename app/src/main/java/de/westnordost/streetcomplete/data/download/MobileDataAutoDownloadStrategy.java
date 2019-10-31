package de.westnordost.streetcomplete.data.download;

import javax.inject.Inject;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider;

public class MobileDataAutoDownloadStrategy extends AActiveRadiusStrategy
{
	@Inject public MobileDataAutoDownloadStrategy(OsmQuestDao osmQuestDB,
												  DownloadedTilesDao downloadedTilesDao,
												  OrderedVisibleQuestTypesProvider questTypesProvider)
	{
		super(osmQuestDB, downloadedTilesDao, questTypesProvider);
	}

	@Override public int getQuestTypeDownloadCount()
	{
		return 4;
	}

	@Override protected int getMinQuestsInActiveRadiusPerKm2()
	{
		return 8;
	}

	@Override protected int[] getActiveRadii()
	{
		return new int[]{300};
	}

	@Override protected int getDownloadRadius()
	{
		return 600;
	}
}
