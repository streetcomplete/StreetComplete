package de.westnordost.streetcomplete.data.download;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

public class MobileDataAutoDownloadStrategy extends AActiveRadiusStrategy
{
	@Inject public MobileDataAutoDownloadStrategy(OsmQuestDao osmQuestDB,
												  DownloadedTilesDao downloadedTilesDao,
												  Provider<List<QuestType>> questTypesProvider)
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
		return new int[]{400};
	}

	@Override protected int getDownloadRadius()
	{
		return 800;
	}
}
