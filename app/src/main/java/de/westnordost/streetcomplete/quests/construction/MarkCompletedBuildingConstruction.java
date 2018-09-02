package de.westnordost.streetcomplete.quests.construction;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.OsmTaggings;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.quests.DateUtil;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class MarkCompletedBuildingConstruction extends MarkCompletedConstruction
{
	@Inject public MarkCompletedBuildingConstruction(OverpassMapDataDao overpassServer) {
		super(overpassServer);
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler);
	}

	/** @return overpass query string to get buildings marked as under construction but excluding ones
	 * - with tagged opening date that is in future
	 * - recently edited (includes adding/updating check_date tags)
	 * . */
	private String getOverpassQuery(BoundingBox bbox)
	{
		String groupName = ".buildings_under_construction";
		String wayGroupName = groupName + "_ways";
		String relationGroupName = groupName + "_relations";
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			"way" + getQueryPart("building", wayGroupName, 180) +
			"relation" + getQueryPart("building", relationGroupName, 180) +
			"(" + wayGroupName + "; " + relationGroupName + ";); " +
			OverpassQLUtil.getQuestPrintStatement();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) {
			String constructionValue = changes.getPreviousValue("construction");
			if(constructionValue == null) {
				constructionValue = "yes";
			}
			changes.modify("building", constructionValue);
			removeTagsDescribingConstruction(changes);
		} else {
			changes.addOrModify(OsmTaggings.SURVEY_MARK_KEY, DateUtil.getCurrentDateString());
		}
	}

	@Override public int getIcon() { return R.drawable.ic_quest_building_construction; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_construction_building_title;
	}
}
