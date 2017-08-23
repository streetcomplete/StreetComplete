package de.westnordost.streetcomplete.data.osm.upload;

import android.content.SharedPreferences;

import javax.inject.Inject;

import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.OsmQuestUnlocker;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

public class UndoOsmQuestChangesUpload extends AOsmQuestChangesUpload
{
	@Inject public UndoOsmQuestChangesUpload(
			MapDataDao osmDao, UndoOsmQuestDao questDB, MergedElementDao elementDB,
			ElementGeometryDao elementGeometryDB, QuestStatisticsDao statisticsDB,
			OpenChangesetsDao openChangesetsDB, ChangesetsDao changesetsDao,
			DownloadedTilesDao downloadedTilesDao, SharedPreferences prefs,
			OsmQuestUnlocker questUnlocker)
	{
		super(osmDao, questDB, elementDB, elementGeometryDB, statisticsDB, openChangesetsDB,
				changesetsDao, downloadedTilesDao, prefs, questUnlocker);
	}
}
