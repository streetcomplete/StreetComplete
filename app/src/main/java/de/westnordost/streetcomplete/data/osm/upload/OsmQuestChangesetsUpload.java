package de.westnordost.streetcomplete.data.osm.upload;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

public class OsmQuestChangesetsUpload extends AOsmQuestChangesetsUpload
{
	@Inject public OsmQuestChangesetsUpload(
		MapDataDao osmDao, OsmQuestDao questDB, MergedElementDao elementDB,
		ElementGeometryDao elementGeometryDB, QuestStatisticsDao statisticsDB,
		OpenChangesetsDao openChangesetsDB, DownloadedTilesDao downloadedTilesDao,
		Provider<OsmQuestChangeUpload> osmQuestChangeUploadProvider,
		ChangesetAutoCloser changesetAutoCloser)
	{
		super(osmDao, questDB, elementDB, elementGeometryDB, statisticsDB, openChangesetsDB,
			downloadedTilesDao, osmQuestChangeUploadProvider, changesetAutoCloser);
	}

	@Override protected String getLogTag()
	{
		return "OsmQuestChangesetsUpload";
	}

	@Override protected boolean shouldCheckForQuestApplicability()
	{
		return true;
	}
}
