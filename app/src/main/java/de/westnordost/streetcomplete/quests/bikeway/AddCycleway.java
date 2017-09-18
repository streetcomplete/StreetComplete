package de.westnordost.streetcomplete.quests.bikeway;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

import static de.westnordost.streetcomplete.quests.bikeway.AddCyclewayForm.Cycleway;

public class AddCycleway implements OsmElementQuestType
{
	private final OverpassMapDataDao overpassServer;

	private static final int MIN_DIST_TO_CYCLEWAYS = 10;

	@Inject public AddCycleway(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		Cycleway cyclewayRight = Cycleway.valueOf(answer.getString(AddCyclewayForm.CYCLEWAY_RIGHT));
		Cycleway cyclewayLeft = Cycleway.valueOf(answer.getString(AddCyclewayForm.CYCLEWAY_LEFT));

		int cyclewayRightDir = answer.getInt(AddCyclewayForm.CYCLEWAY_RIGHT_DIR);
		int cyclewayLeftDir = answer.getInt(AddCyclewayForm.CYCLEWAY_LEFT_DIR);

		if(cyclewayLeft == cyclewayRight && cyclewayRightDir == 0 && cyclewayLeftDir == 0)
		{
			applyCyclewayAnswerTo(cyclewayLeft, Side.BOTH, 0, changes);
		}
		else
		{
			applyCyclewayAnswerTo(cyclewayLeft, Side.LEFT, cyclewayLeftDir, changes);
			applyCyclewayAnswerTo(cyclewayRight, Side.RIGHT, cyclewayRightDir, changes);
		}
	}

	private enum Side { LEFT, RIGHT, BOTH }

	private void applyCyclewayAnswerTo(Cycleway cycleway, Side side, int dir, StringMapChangesBuilder changes)
	{
		String directionValue = null;
		if(dir != 0) directionValue = dir > 0 ? "yes" : "-1";

		String cyclewayKey = "cycleway" + getSideKey(side);
		switch (cycleway)
		{
			case NONE:
				changes.add(cyclewayKey, "no");
				break;
			case LANE:
				changes.add(cyclewayKey, "lane");
				if(directionValue != null)
				{
					changes.addOrModify(cyclewayKey + ":oneway", directionValue);
				}
				break;
			case TRACK:
				changes.add(cyclewayKey, "track");
				if(directionValue != null)
				{
					changes.addOrModify(cyclewayKey + ":oneway", directionValue);
				}
				break;
			case TRACK_DUAL:
				changes.add(cyclewayKey, "track");
				changes.addOrModify(cyclewayKey + ":oneway", "no");
				break;
			case LANE_DUAL:
				changes.add(cyclewayKey, "lane");
				changes.addOrModify(cyclewayKey + ":oneway", "no");
				break;
			case SIDEWALK:
				// https://wiki.openstreetmap.org/wiki/File:Z240GemeinsamerGehundRadweg.jpeg
				changes.add(cyclewayKey, "track");
				changes.add(cyclewayKey + ":segregated", "no");
				break;
			case SIDEWALK_OK:
				// https://wiki.openstreetmap.org/wiki/File:Z239Z1022-10GehwegRadfahrerFrei.jpeg
				changes.add(cyclewayKey, "no");
				changes.addOrModify("sidewalk", getSideValue(side));
				changes.add("sidewalk" + getSideKey(side) + ":bicycle", "yes");
				break;
			case SHARED:
				changes.add(cyclewayKey, "shared_lane");
				break;
			case BUSWAY:
				changes.add(cyclewayKey, "share_busway");
				break;
		}
	}

	private static String getSideKey(Side side)
	{
		switch (side)
		{
			case LEFT:   return ":left";
			case RIGHT:  return ":right";
			default:     return "";
		}
	}

	private static String getSideValue(Side side)
	{
		switch (side)
		{
			case LEFT:   return "left";
			case RIGHT:  return "right";
			default:     return "both";
		}
	}

	@Override public boolean appliesTo(Element element)
	{
		/* Whether this element applies to this quest cannot be determined by looking at that
		   element alone (see download()), an Overpass query would need to be made to find this out.
		   This is too heavy-weight for this method so it always returns false. */

		/* The implications of this are that this quest will never be created directly
		   as consequence of solving another quest and also after reverting an input,
		   the quest will not immediately pop up again. Instead, they are downloaded well after an
		   element became fit for this quest. */
		return false;
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler);
	}

	/** @return overpass query string to get streets without cycleway info not near paths roads that don't have names */
	private static String getOverpassQuery(BoundingBox bbox)
	{
		int d = MIN_DIST_TO_CYCLEWAYS;
		return OverpassQLUtil.getOverpassBBox(bbox) +
			"way[highway ~ \"^(primary|secondary|tertiary|unclassified|residential)$\"]" +
			   "[!cycleway][!\"cycleway:left\"][!\"cycleway:right\"]" +
			   "[!\"sidewalk:bicycle\"][!\"sidewalk:left:bicycle\"][!\"sidewalk:right:bicycle\"]" +
			   " -> .streets;" +
			"(" +
			   "way[highway=cycleway](around.streets: "+d+");" +
			   "way[highway ~ \"^(path|footway)$\"][bicycle ~ \"^(yes|designated)$\"](around.streets: "+d+");" +
			") -> .cycleways;" +
		    "way.streets(around.cycleways: "+d+") -> .streets_near_cycleways;" +
		    "(.streets; - .streets_near_cycleways;);" +
			"out meta geom;";
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new AddCyclewayForm(); }
	@Override public String getCommitMessage() { return "Add whether there are cycleways"; }
	@Override public int getIcon() { return R.drawable.ic_quest_bicycleway; }
	@Override public int getTitle(@NonNull Map<String, String> tags) { return getTitle(); }
	@Override public int getTitle() { return R.string.quest_cycleway_title; }

	// TODO unit test!
}
