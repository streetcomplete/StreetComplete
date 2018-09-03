package de.westnordost.streetcomplete.quests.oneway;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

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
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.OsmLatLon;
import de.westnordost.streetcomplete.util.StreamUtils;

/** Dao for using this API: https://github.com/ENT8R/oneway-data-api */
public class TrafficFlowSegmentsDao
{
	private final String apiUrl;

	public TrafficFlowSegmentsDao(String url)
	{
		apiUrl = url;
	}

	@NonNull
	public Map<Long, List<TrafficFlowSegment>> get(BoundingBox bbox) throws IOException, JSONException
	{
		URL url = new URL(apiUrl + "?bbox=" + bbox.getAsLeftBottomRightTopString());
		String json = StreamUtils.readToString(url.openConnection().getInputStream());
		return parse(json);
	}

	@NonNull static Map<Long, List<TrafficFlowSegment>> parse(String json) throws JSONException
	{
		JSONObject obj = new JSONObject(json);
		JSONArray segments = obj.getJSONArray("segments");

		@SuppressLint("UseSparseArrays")
		Map<Long, List<TrafficFlowSegment>> result = new HashMap<>();
		if(segments == null) return result;

		for (int i = 0; i < segments.length(); i++)
		{
			if(segments.isNull(i)) continue;
			JSONObject segment = segments.getJSONObject(i);
			long wayId = segment.getLong("wayId");
			if(result.get(wayId) == null)
			{
				result.put(wayId, new ArrayList<>());
			}

			result.get(wayId).add(new TrafficFlowSegment(
				parseLatLon(segment.getJSONObject("fromPosition")),
				parseLatLon(segment.getJSONObject("toPosition"))
			));
		}
		return result;
	}

	private static LatLon parseLatLon(JSONObject pos) throws JSONException
	{
		return new OsmLatLon(pos.getDouble("lat"),pos.getDouble("lon"));
	}
}
