package de.westnordost.streetcomplete.quests.construction;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

public class MarkCompletedHighwayConstruction extends MarkCompletedConstruction
{
	@Inject
	public MarkCompletedHighwayConstruction(OverpassMapDataDao overpassServer) {
		super(overpassServer);
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler);
	}

	/** @return overpass query string to get streets marked as under construction but excluding ones
	 * - with invalid construction tag
	 * - with tagged opening date that is in future
	 * - recently edited (includes adding/updating check_date tags)
	 * - with fixme (as in some situations this quest may add fixme note as an edit)
	 * . */
	private String getOverpassQuery(BoundingBox bbox)
	{
		String groupName = ".roads_under_construction";
		String resultsName = ".roads_for_review";
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			"way" + getQueryPart("highway", groupName, 14) +
			"(" +
			"  way[highway=construction][fixme];" +
			") -> .with_fixme;" +
			"(" +
			groupName + " - .with_fixme;" +
			") -> " + resultsName + ";" +
			resultsName + " out meta geom;";
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) {
			if(isAnyAccessTagPresent(changes)){
				// there are some access tag present that are likely to be temporary
				// and valid only during construction - marking construction as finished
				// would likely cause nonobvious tagging mistake
				changes.add("fixme", "construction is completed but there are access tags that maybe were true only during construction period");
				return;
			}
			String constructionValue = changes.getPreviousValue("construction");
			if(constructionValue == null) {
				constructionValue = "road";
			}
			changes.modify("highway", constructionValue);
			removeTagsDescribingConstruction(changes);
		} else {
			changes.addOrModify(OsmTaggings.SURVEY_MARK_KEY, DateUtil.getCurrentDateString());
		}
	}

	private boolean isAnyAccessTagPresent(StringMapChangesBuilder changes){
		for(String accessTag : OsmTaggings.POPULAR_ROAD_ACCESS_TAGS){
			if(changes.getPreviousValue(accessTag) != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getIcon() {
		return R.drawable.ic_quest_road_construction;
	}

	@Override
	public int getTitle() {
		return R.string.quest_construction_road_title;
	}

	@Override public int getTitle(@NonNull Map<String, String> tags) {
		if (Arrays.asList(OsmTaggings.ALL_ROADS).contains(tags.get("construction"))){
			return R.string.quest_construction_road_title;
		} else if (tags.get("construction") == "cycleway") {
			return R.string.quest_construction_cycleway_title;
		} else if (tags.get("construction") == "footway") {
			return R.string.quest_construction_footway_title;
		}
		return R.string.quest_construction_generic_title;
	}
}
