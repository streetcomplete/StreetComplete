package de.westnordost.streetcomplete.data.download;

import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

public class MobileDataAutoDownloadStrategy extends AActiveRadiusStrategy
{
	@Inject public MobileDataAutoDownloadStrategy(OsmQuestDao osmQuestDB,
												  DownloadedTilesDao downloadedTilesDao,
												  List<QuestType> questTypes)
	{
		super(osmQuestDB, downloadedTilesDao, questTypes);
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
