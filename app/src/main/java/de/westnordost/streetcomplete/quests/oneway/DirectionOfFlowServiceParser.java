package de.westnordost.streetcomplete.quests.oneway;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DirectionOfFlowServiceParser
{
	public List<DirectionOfFlow> parse(String json) throws JSONException
	{
		JSONObject obj = new JSONObject(json);
		JSONArray segments = obj.getJSONArray("segments");

		List<DirectionOfFlow> directionOfFlows = new ArrayList<>(segments.length());
		for (int i = 0; i < segments.length(); i++)
		{
			JSONObject segment = segments.getJSONObject(i);
			directionOfFlows.add(new DirectionOfFlow(
				segment.getLong("wayId"),
				segment.getLong("fromNodeId"),
				segment.getLong("toNodeId")
			));
		}
		return directionOfFlows;
	}
}
