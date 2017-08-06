package de.westnordost.streetcomplete.quests.way_lit;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;


public class AddWayLit implements OsmElementQuestType
{
	private static final int SIDEWALK_SEARCH_RADIUS = 100; // meters

	static final String[] LIT_ROADS = {
			"primary", "secondary", "tertiary", "unclassified", "residential", "living_street",
			"service", "pedestrian"
	};

	static final String[] LIT_WAYS = {"footway", "cycleway"};

	private final OverpassMapDataDao overpassServer;

	@Inject public AddWayLit(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return overpassServer.getAndHandleQuota(getOverpassBBox(bbox) + getOverpassQuery(), handler);
	}

	private static String getOverpassQuery()
	{
		/* Using sidewalk as a tell-tale tag for (urban) streets which reached a certain level of
			development. I.e. non-urban streets will usually not even be lit in industrialized
			countries.
			Also, only include paths only for those which are equal to footway/cycleway to exclude
			most hike paths and trails.

			See #427 for discussion. */
		return "(" +
				" way[highway ~ \"" + TextUtils.join("|", LIT_WAYS) + "\"];" +
				" way[highway = path][foot = designated];" +
				" way[highway = path][bicycle = designated];" +
				") -> .sidewalks;" +
				"way[highway ~ \"" + TextUtils.join("|", LIT_ROADS) + "\"] -> .streets;" +
				"(" +
				" way.streets(around.sidewalks:" + SIDEWALK_SEARCH_RADIUS + ")[!lit];" +
				" way.sidewalks[!lit];" +
				");" +
				"out meta geom;";
	}

	private static String getOverpassBBox(BoundingBox bbox)
	{
		return "[bbox:" +
				bbox.getMinLatitude() + "," + bbox.getMinLongitude() + "," +
				bbox.getMaxLatitude() + "," + bbox.getMaxLongitude() +
				"];";
	}


	public AbstractQuestAnswerFragment createForm()
	{
		return new WayLitForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String other = answer.getString(WayLitForm.OTHER_ANSWER);
		if (other != null)
		{
			changes.add("lit", other);
		}
		else
		{
			changes.add("lit", answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no");
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add way lit";
	}


	@Override public String getIconName()
	{
		return "lantern";
	}
}
