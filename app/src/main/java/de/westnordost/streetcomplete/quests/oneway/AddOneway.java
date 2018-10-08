package de.westnordost.streetcomplete.quests.oneway;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.AOsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddOneway extends AOsmElementQuestType
{
	private static final String TAG = "AddOneway";

	private final OverpassMapDataDao overpassMapDataDao;
	private final WayTrafficFlowDao db;
	private final TrafficFlowSegmentsDao trafficFlowSegmentsDao;

	private static final TagFilterExpression FILTER = new FiltersParser().parse(
		" ways with highway ~ " +
			"service|living_street|residential|unclassified|tertiary|secondary|primary|trunk|" +
			"road|tertiary_link|secondary_link|primary_link|trunk_link" +
		" and !oneway and access !~ private|no and area != yes"
	);

	public AddOneway(OverpassMapDataDao overpassMapDataDao,
					 TrafficFlowSegmentsDao trafficFlowSegmentsDao, WayTrafficFlowDao db)
	{
		this.overpassMapDataDao = overpassMapDataDao;
		this.db = db;
		this.trafficFlowSegmentsDao = trafficFlowSegmentsDao;
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		Map<Long, List<TrafficFlowSegment>> trafficDirectionMap;
		try
		{
			trafficDirectionMap = trafficFlowSegmentsDao.get(bbox);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Unable to download traffic metadata", e);
			return false;
		}
		if(trafficDirectionMap.isEmpty()) return true;

		String overpassQuery = "way(id:" + TextUtils.join(",",trafficDirectionMap.keySet()) + "); out meta geom;";
		overpassMapDataDao.getAndHandleQuota(overpassQuery, (element, geometry) ->
		{
			if(geometry == null) return;
			// filter the data as ImproveOSM data may be outdated or catching too much
			if(!FILTER.matches(element)) return;

			Way way = (Way) element;
			List<TrafficFlowSegment> segments = trafficDirectionMap.get(way.getId());
			if(segments == null) return;
			Boolean isForward = isForward(geometry, segments);
			// only create quest if direction can be clearly determined and is the same direction
			// for all segments belonging to one OSM way (because StreetComplete cannot split ways
			// up)
			if(isForward == null) return;

			db.put(way.getId(), isForward);
			handler.handle(element, geometry);
		});

		return true;
	}

	/** @param geometry the OSM geometry
	 *  @param trafficFlowSegments list of segments which document a road user flow
	 *  @return true if all given segments point forward in relation to the direction of the OSM way
	 *          and false if they all point backward.<br>
	 *          If the segments point into different directions or their direction cannot be
	 *          determined. returns null. */
	@Nullable private static Boolean isForward(@NonNull ElementGeometry geometry,
											   @NonNull List<TrafficFlowSegment> trafficFlowSegments)
	{
		Boolean result = null;
		List<LatLon> positions = geometry.polylines.get(0);
		for (TrafficFlowSegment segment : trafficFlowSegments)
		{
			int fromPositionIndex = findClosestPositionIndexOf(positions, segment.fromPosition);
			int toPositionIndex = findClosestPositionIndexOf(positions, segment.toPosition);

			if(fromPositionIndex == -1 || toPositionIndex == -1) return null;
			if(fromPositionIndex == toPositionIndex) return null;

			boolean forward = fromPositionIndex < toPositionIndex;
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

	private static int findClosestPositionIndexOf(List<LatLon> positions, LatLon latlon)
	{
		double shortestDistance = 1;
		int result = -1, index = 0;
		for (LatLon pos : positions)
		{
			double distance = Math.hypot(
				pos.getLongitude() - latlon.getLongitude(),
				pos.getLatitude() - latlon.getLatitude());
			if(distance < 0.00005 && distance < shortestDistance) {
				shortestDistance = distance;
				result = index;
			}
			index++;
		}

		return result;
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

	@Override public Boolean isApplicableTo(Element element)
	{
		return FILTER.matches(element) && db.isForward(element.getId()) != null;
	}

	@Override public void cleanMetadata()
	{
		db.deleteUnreferenced();
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new AddOnewayForm(); }
	@Override public String getCommitMessage() {
		return "Add whether this road is a one-way road," +
			" this road was marked as likely oneway by improveosm.org";
	}
	@Override public int getIcon() { return R.drawable.ic_quest_oneway; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_oneway_title;
	}
}
