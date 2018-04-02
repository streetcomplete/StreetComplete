package de.westnordost.streetcomplete.quests.oneway;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;
import de.westnordost.streetcomplete.util.StreamUtils;

public class AddOneway implements OsmElementQuestType
{
	private static final String TAG = "AddOneway";

	private static final String URL = "https://directionofflow.skobbler.net/directionOfFlowService/search";

	private final OverpassMapDataDao overpassMapDataDao;
	private final TrafficFlowDao db;

	private static final TagFilterExpression FILTER = new FiltersParser().parse(
		" ways with highway ~ " +
			"service|living_street|residential|unclassified|tertiary|secondary|primary|trunk|" +
			"road|tertiary_link|secondary_link|primary_link|trunk_link" +
		" and !oneway and access !~ private|no"
	);

	public AddOneway(OverpassMapDataDao overpassMapDataDao, TrafficFlowDao db)
	{
		this.overpassMapDataDao = overpassMapDataDao;
		this.db = db;
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		Map<Long, List<TrafficFlowSegment>> trafficDirectionMap;
		try
		{
			trafficDirectionMap = downloadTrafficFlowSegments(bbox);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Unable to download traffic metadata", e);
			return false;
		}

		String overpassQuery = "way(id:" + TextUtils.join(",",trafficDirectionMap.keySet()) + ");";
		overpassMapDataDao.getAndHandleQuota(overpassQuery, (element, geometry) ->
		{
			// filter the data as ImproveOSM data may be outdated or catching too much
			if(!FILTER.matches(element)) return;

			Way way = (Way) element;
			List<TrafficFlowSegment> segments = trafficDirectionMap.get(way.getId());
			if(segments == null) return;
			Boolean isForward = isForward(way, segments);
			// only create quest if direction can be clearly determined and is the same direction
			// for all segments belonging to one OSM way (because StreetComplete cannot split ways
			// up)
			if(isForward == null) return;

			db.put(way.getId(), isForward);
			handler.handle(element, geometry);
		});

		return true;
	}

	@NonNull private Map<Long, List<TrafficFlowSegment>> downloadTrafficFlowSegments(BoundingBox bbox) throws IOException, JSONException
	{
		String[] paramBounds = {
			"south="+bbox.getMinLatitude(),	"north="+bbox.getMaxLatitude(),
			"west="+bbox.getMinLongitude(),	"east="+bbox.getMaxLongitude()
		};

		java.net.URL url = new URL(URL + "?" + TextUtils.join("&", paramBounds));
		String json = StreamUtils.readToString(url.openConnection().getInputStream());
		return parseTrafficFlowSegments(json);
	}

	@NonNull static Map<Long, List<TrafficFlowSegment>> parseTrafficFlowSegments(String json) throws JSONException
	{
		// we are only interested in the fromNodeId->toNodeId segments and that's it
		JSONObject obj = new JSONObject(json);
		JSONArray segments = obj.getJSONArray("segments");

		@SuppressLint("UseSparseArrays")
		Map<Long, List<TrafficFlowSegment>> result = new HashMap<>();
		if(segments == null) return result;

		for (int i = 0; i < segments.length(); i++)
		{
			JSONObject segment = segments.getJSONObject(i);
			long wayId = segment.getLong("wayId");
			if(result.get(wayId) == null)
			{
				result.put(wayId, new ArrayList<>());
			}
			result.get(wayId).add(new TrafficFlowSegment(
				segment.getLong("fromNodeId"),
				segment.getLong("toNodeId")
			));
		}
		return result;
	}

	/** @param way the OSM way
	 *  @param trafficFlowSegments list of segments which document a road user flow
	 *  @return true if all given segments point forward in relation to the direction of the OSM way
	 *  		and false if they all point backward.<br>
	 *          If the segments point into different directions or their direction cannot be
	 *          determined. returns null. */
	@Nullable private static Boolean isForward(@NonNull Way way, @NonNull List<TrafficFlowSegment> trafficFlowSegments)
	{
		Boolean result = null;
		List<Long> nodes = way.getNodeIds();
		for (TrafficFlowSegment segment : trafficFlowSegments)
		{
			int fromNodeIndex = nodes.indexOf(segment.fromNodeId);
			int toNodeIndex = nodes.indexOf(segment.toNodeId);

			if(fromNodeIndex == -1 || toNodeIndex == -1) return null;
			if(fromNodeIndex == toNodeIndex) return null;

			boolean forward = fromNodeIndex < toNodeIndex;
			if(result == null)
			{
				result = forward;
			}
			else if(result != forward)
			{
				return null;
			}
		}
		return result;
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddOnewayForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		boolean isOneway = answer.getBoolean(AddOnewayForm.ANSWER);
		if(!isOneway)
		{
			changes.add("oneway","no");
		}
		else
		{
			long wayId = answer.getLong(AddOnewayForm.WAY_ID);
			changes.add("oneway", db.isForward(wayId) ? "yes" : "-1");
		}
	}

	// TODO must remove metadata from DB after upload!!

	@Override public Boolean isApplicableTo(Element element)
	{
		return FILTER.matches(element) && db.isForward(element.getId()) != null;
	}

	@Override public String getCommitMessage() { return "Add whether this road is a one-way road"; }
	@Override public int getIcon() { return R.drawable.ic_quest_oneway; }
	@Override public int getTitle() { return R.string.quest_oneway_title; }
	@Override public int getDefaultDisabledMessage() { return 0; }
	@Override public int getTitle(@NonNull Map<String, String> tags) { return getTitle(); }
	@NonNull @Override public Countries getEnabledForCountries() { return Countries.ALL; }
}
