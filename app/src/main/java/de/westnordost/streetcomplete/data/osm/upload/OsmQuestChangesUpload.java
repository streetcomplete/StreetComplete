package de.westnordost.streetcomplete.data.osm.upload;

import android.content.SharedPreferences;

import javax.inject.Inject;

import de.westnordost.osmapi.changesets.ChangesetsDao;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;
import de.westnordost.streetcomplete.data.osm.OsmQuest;
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver;
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao;
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao;
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao;
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao;
import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao;

public class OsmQuestChangesUpload extends AOsmQuestChangesUpload
{
	@Inject public OsmQuestChangesUpload(
			MapDataDao osmDao, OsmQuestDao questDB, MergedElementDao elementDB,
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
		return "OsmQuestUpload";
	}

	@Override protected boolean questIsApplicableToElement(OsmQuest quest, Element element)
	{
		Boolean questIsApplicableToElement = quest.getOsmElementQuestType().isApplicableTo(element);
		return questIsApplicableToElement == null || questIsApplicableToElement;
	}
}
