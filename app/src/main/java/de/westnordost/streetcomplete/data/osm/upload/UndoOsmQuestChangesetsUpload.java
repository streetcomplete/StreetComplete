package de.westnordost.streetcomplete.data.osm.upload;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

public class UndoOsmQuestChangesetsUpload extends AOsmQuestChangesetsUpload
{
	@Inject public UndoOsmQuestChangesetsUpload(
			MapDataDao osmDao, UndoOsmQuestDao questDB, MergedElementDao elementDB,
			ElementGeometryDao elementGeometryDB, QuestStatisticsDao statisticsDB,
			OpenChangesetsDao openChangesetsDB, DownloadedTilesDao downloadedTilesDao,
			@Named("undo") Provider<OsmQuestChangeUpload> osmQuestChangeUploadProvider,
			ChangesetAutoCloser changesetAutoCloser)
	{
		super(osmDao, questDB, elementDB, elementGeometryDB, statisticsDB, openChangesetsDB,
			downloadedTilesDao, osmQuestChangeUploadProvider, changesetAutoCloser);
	}

	@Override protected String getLogTag()
	{
		return "UndoOsmQuestChangesetsUpload";
	}

	@Override protected boolean shouldCheckForQuestApplicability()
	{
		// can't ask the quest here if it is applicable to the element or not, because the change
		// of the revert is exactly the opposite of what the quest would normally change and the
		// element ergo has the changes already applied that a normal quest would add
		return false;
	}
}
