package de.westnordost.streetcomplete.quests.construction;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.OsmTaggings;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.DateHandler;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class MarkCompletedConstruction  implements OsmElementQuestType
{
	private final OverpassMapDataDao overpassServer;

	@Inject
	public MarkCompletedConstruction(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Nullable
	@Override public Boolean isApplicableTo(Element element)
	{
		/* Whether this element applies to this quest cannot be determined by looking at that
		   element alone (see download()), an Overpass query would need to be made to find this out.
		   This is too heavy-weight for this method so it always returns false. */

		/* The implications of this are that this quest will never be created directly
		   as consequence of solving another quest and also after reverting an input,
		   the quest will not immediately pop up again. Instead, they are downloaded well after an
		   element became fit for this quest. */
		return null;
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
	private static String getOverpassQuery(BoundingBox bbox)
	{
		String currentDate = DateHandler.getCurrentDateString() + "T00:00:00Z";
		String twoWeeksAgo = DateHandler.getOffsetDateString(-14) + "T00:00:00Z";

		ArrayList<String> acceptedConstructionValues = new ArrayList<String>(Arrays.asList(OsmTaggings.ALL_ROADS));
		acceptedConstructionValues.add("cycleway");
		acceptedConstructionValues.add("footway");
		acceptedConstructionValues.add("path");

		String query = OverpassQLUtil.getOverpassBBox(bbox) +
			"way[highway=construction] -> .all_roads_under_construction;" +
			"(" +
			"way[construction]" +
				"[construction !~ \"^("+ TextUtils.join("|", acceptedConstructionValues)+")$\"]" +
			") -> .invalid_construction_type;" +
			"(" +
			"way[highway=construction][opening_date](" +
			"  if:is_date(t['opening_date']) && date(t['opening_date'])>date('" + currentDate + "'));" +
			") -> .known_opening_date_in_future;" +
			"(" +
			"  way[highway=construction](newer:'" + twoWeeksAgo + "');" +
			") -> .recently_edited;" +
			"(" +
			"  way[highway=construction][fixme];" +
			") -> .with_fixme;" +
			"(" +
			"(((" +
			"	.all_roads_under_construction" +
			"	- .known_opening_date_in_future)" +
			"	- .recently_edited)" +
			"	- .with_fixme)" +
			"	- .invalid_construction_type;" +
			");" +
			"out meta geom;";
		Log.e("tag", query);
		return query;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) {
			if(isAnyAccessTagPresent(changes)){
				// there are some access tag present that are likely to be temporary
				// and valid only during construction - marking construction as finished
				// would likely cause nonobvious tagging mistake
				changes.add("fixme", "construction is completed but there are access tags that may be true only during construction period");
				return;
			}
			String constructionValue = changes.getPreviousValue("construction");
			if(constructionValue == null) {
				constructionValue = "road";
			}
			changes.deleteIfPresent("construction");
			changes.deleteIfPresent("source:construction");
			changes.modify("highway", constructionValue);
			changes.deleteIfPresent("opening_date");
			changes.deleteIfPresent("source:opening_date");
			changes.deleteIfPresent(OsmTaggings.SURVEY_MARK_KEY);
		} else {
			changes.addOrModify(OsmTaggings.SURVEY_MARK_KEY, DateHandler.getCurrentDateString());
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

	@Override public String getCommitMessage() { return "Add whether construction is now completed"; }

	@Override public int getIcon() { return R.drawable.ic_quest_ferry; } //TODO, use a better icon

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

	@NonNull
	@Override
	public Countries getEnabledForCountries() {
		return Countries.ALL;
	}

	@Override
	public int getDefaultDisabledMessage() {
		return 0;
	}
}
