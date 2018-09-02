package de.westnordost.streetcomplete.quests.bikeway;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.meta.OsmTaggings;
import de.westnordost.streetcomplete.data.osm.AOsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

import static de.westnordost.streetcomplete.quests.bikeway.Cycleway.EXCLUSIVE_LANE;
import static de.westnordost.streetcomplete.quests.bikeway.Cycleway.ADVISORY_LANE;

public class AddCycleway extends AOsmElementQuestType
{
	private final OverpassMapDataDao overpassServer;

	private static final int MIN_DIST_TO_CYCLEWAYS = 15; //m

	@Inject public AddCycleway(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String right = answer.getString(AddCyclewayForm.CYCLEWAY_RIGHT);
		String left = answer.getString(AddCyclewayForm.CYCLEWAY_LEFT);

		Cycleway cyclewayRight = right != null ? Cycleway.valueOf(right) : null;
		Cycleway cyclewayLeft = left != null ? Cycleway.valueOf(left) : null;

		int cyclewayRightDir = answer.getInt(AddCyclewayForm.CYCLEWAY_RIGHT_DIR);
		int cyclewayLeftDir = answer.getInt(AddCyclewayForm.CYCLEWAY_LEFT_DIR);

		boolean bothSidesAreSame = cyclewayLeft == cyclewayRight
				&& cyclewayRightDir == 0 && cyclewayLeftDir == 0;

		if(bothSidesAreSame)
		{
			applyCyclewayAnswerTo(cyclewayLeft, Side.BOTH, 0, changes);
		}
		else
		{
			if(cyclewayLeft != null)
			{
				applyCyclewayAnswerTo(cyclewayLeft, Side.LEFT, cyclewayLeftDir, changes);
			}
			if(cyclewayRight != null)
			{
				applyCyclewayAnswerTo(cyclewayRight, Side.RIGHT, cyclewayRightDir, changes);
			}
		}

		applySidewalkAnswerTo(cyclewayLeft, cyclewayRight, changes);

		if(answer.getBoolean(AddCyclewayForm.IS_ONEWAY_NOT_FOR_CYCLISTS))
		{
			changes.addOrModify("oneway:bicycle", "no");
		}
	}

	private void applySidewalkAnswerTo(Cycleway cyclewayLeft, Cycleway cyclewayRight,
									   StringMapChangesBuilder changes)
	{
		boolean hasSidewalkLeft = cyclewayLeft != null && cyclewayLeft.isOnSidewalk();
		boolean hasSidewalkRight = cyclewayRight != null && cyclewayRight.isOnSidewalk();

		Side side;
		if(hasSidewalkLeft && hasSidewalkRight)	side = Side.BOTH;
		else if(hasSidewalkLeft)				side = Side.LEFT;
		else if(hasSidewalkRight)				side = Side.RIGHT;
		else									side = null;

		if(side != null)
		{
			changes.addOrModify("sidewalk", side.value);
		}
	}

	private enum Side
	{
		LEFT("left"), RIGHT("right"), BOTH("both");

		public final String value;
		Side(String value) { this.value = value; }
	}

	private void applyCyclewayAnswerTo(Cycleway cycleway, Side side, int dir, StringMapChangesBuilder changes)
	{
		String directionValue = null;
		if(dir != 0) directionValue = dir > 0 ? "yes" : "-1";

		String cyclewayKey = "cycleway:" + side.value;
		switch (cycleway)
		{
			case NONE:
			case NONE_NO_ONEWAY:
				changes.add(cyclewayKey, "no");
				break;
			case EXCLUSIVE_LANE:
			case ADVISORY_LANE:
			case LANE_UNSPECIFIED:
				changes.add(cyclewayKey, "lane");
				if(directionValue != null)
				{
					changes.addOrModify(cyclewayKey + ":oneway", directionValue);
				}
				if(cycleway == EXCLUSIVE_LANE)  changes.addOrModify(cyclewayKey + ":lane", "exclusive");
				else if(cycleway == ADVISORY_LANE) changes.addOrModify(cyclewayKey + ":lane", "advisory");
				break;
			case TRACK:
				changes.add(cyclewayKey, "track");
				if(directionValue != null)
				{
					changes.addOrModify(cyclewayKey + ":oneway", directionValue);
				}
				break;
			case DUAL_TRACK:
				changes.add(cyclewayKey, "track");
				changes.addOrModify(cyclewayKey + ":oneway", "no");
				break;
			case DUAL_LANE:
				changes.add(cyclewayKey, "lane");
				changes.addOrModify(cyclewayKey + ":oneway", "no");
				changes.addOrModify(cyclewayKey + ":lane", "exclusive");
				break;
			case SIDEWALK_EXPLICIT:
				// https://wiki.openstreetmap.org/wiki/File:Z240GemeinsamerGehundRadweg.jpeg
				changes.add(cyclewayKey, "track");
				changes.add(cyclewayKey + ":segregated", "no");
				break;
			case SIDEWALK_OK:
				// https://wiki.openstreetmap.org/wiki/File:Z239Z1022-10GehwegRadfahrerFrei.jpeg
				changes.add(cyclewayKey, "no");
				changes.add("sidewalk:" + side.value + ":bicycle", "yes");
				break;
			case PICTOGRAMS:
				changes.add(cyclewayKey, "shared_lane");
				changes.add(cyclewayKey + ":lane", "pictogram");
				break;
			case SUGGESTION_LANE:
				changes.add(cyclewayKey, "shared_lane");
				changes.add(cyclewayKey + ":lane", "advisory");
				break;
			case BUSWAY:
				changes.add(cyclewayKey, "share_busway");
				break;
		}
	}

	@Nullable @Override public Boolean isApplicableTo(Element element)
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

	/** @return overpass query string to get streets without cycleway info not near paths for
	 *  bicycles. */
	private static String getOverpassQuery(BoundingBox bbox)
	{
		int d = MIN_DIST_TO_CYCLEWAYS;
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			"way[highway ~ \"^(primary|secondary|tertiary|unclassified)$\"]" +
			   "[area != yes]" +
				// only without cycleway tags
			   "[!cycleway][!\"cycleway:left\"][!\"cycleway:right\"][!\"cycleway:both\"]" +
			   "[!\"sidewalk:bicycle\"][!\"sidewalk:both:bicycle\"][!\"sidewalk:left:bicycle\"][!\"sidewalk:right:bicycle\"]" +
			   // not any with low speed limit because they not very likely to have cycleway infrastructure
			   "[maxspeed !~ \"^(20|15|10|8|7|6|5|10 mph|5 mph|walk)$\"]" +
			   // not any unpaved because of the same reason
			   "[surface !~ \"^("+ TextUtils.join("|", OsmTaggings.ANYTHING_UNPAVED)+")$\"]" +
			   // not any explicitly tagged as no bicycles
			   "[bicycle != no]" +
			   "[access !~ \"^private|no$\"]" +
				// some roads may be father than MIN_DIST_TO_CYCLEWAYS from cycleways,
				// not tagged cycleway=separate/sidepath but may have hint that there is
				// a separately tagged cycleway
				"[bicycle != use_sidepath][\"bicycle:backward\" != use_sidepath]" +
				"[\"bicycle:forward\" != use_sidepath]" +
			   " -> .streets;" +
			"(" +
			   "way[highway=cycleway](around.streets: "+d+");" +
			   // See #718: If a separate way exists, it may be that the user's answer should
			   // correctly be tagged on that separate way and not on the street -> this app would
			   // tag data on the wrong elements. So, don't ask at all for separately mapped ways.
			   // :-(
			   "way[highway ~ \"^(path|footway)$\"](around.streets: "+d+");" +
			") -> .cycleways;" +
		    "way.streets(around.cycleways: "+d+") -> .streets_near_cycleways;" +
		    "(.streets; - .streets_near_cycleways;);" +
			OverpassQLUtil.getQuestPrintStatement();
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new AddCyclewayForm(); }
	@Override public String getCommitMessage() { return "Add whether there are cycleways"; }
	@Override public int getIcon() { return R.drawable.ic_quest_bicycleway; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_cycleway_title2;
	}

	@NonNull @Override public Countries getEnabledForCountries()
	{
		// See overview here: https://ent8r.github.io/blacklistr/?java=bikeway/AddCycleway.java

		// #749. sources:
		// Google Street View (driving around in virtual car)
		// https://en.wikivoyage.org/wiki/Cycling
		// http://peopleforbikes.org/get-local/ (US)
		return Countries.noneExcept(new String[]
		{
			// all of Northern and Western Europe, most of Central Europe, some of Southern Europe
			"NO","SE","FI","IS","DK",
			"GB","IE","NL","BE","FR","LU",
			"DE","PL","CZ","HU","AT","CH","LI",
			"ES","IT",
			// East Asia
			"JP","KR","TW",
			// some of China (East Coast)
			"CN-BJ","CN-TJ","CN-SD","CN-JS","CN-SH",
			"CN-ZJ","CN-FJ","CN-GD","CN-CQ",
			// Australia etc
			"NZ","AU",
			// some of Canada
			"CA-BC","CA-QC","CA-ON","CA-NS","CA-PE",
			// some of the US
			// West Coast, East Coast, Center, South
			"US-WA","US-OR","US-CA",
			"US-MA","US-NJ","US-NY","US-DC","US-CT","US-FL",
			"US-MN","US-MI","US-IL","US-WI","US-IN",
			"US-AZ","US-TX",
		});
	}
}
