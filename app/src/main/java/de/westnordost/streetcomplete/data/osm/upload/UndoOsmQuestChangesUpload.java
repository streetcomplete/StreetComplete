package de.westnordost.streetcomplete.data.osm.upload;

import android.content.SharedPreferences;

import javax.inject.Inject;

import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver;
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
			OsmQuestGiver questUnlocker)
	{
		super(osmDao, questDB, elementDB, elementGeometryDB, statisticsDB, openChangesetsDB,
				changesetsDao, downloadedTilesDao, prefs, questUnlocker);
	}

	@Override protected String getLogTag()
	{
		return "UndoOsmQuestUpload";
	}

	@Override protected boolean questIsApplicableToElement(OsmQuest quest, Element element)
	{
		// can't ask the quest here if it is applicable to the element or not, because the change
		// of the revert is exactly the opposite of what the quest would normally change and the
		// element ergo has the changes already applied that a normal quest would add
		return true;
	}
}
