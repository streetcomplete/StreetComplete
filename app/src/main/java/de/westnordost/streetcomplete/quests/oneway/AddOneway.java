package de.westnordost.streetcomplete.quests.oneway;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.westnordost.osmapi.map.MapDataDao;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddOneway implements OsmElementQuestType
{
	private final OverpassMapDataDao overpassMapDataDao;

	public AddOneway(OverpassMapDataDao overpassMapDataDao)
	{
		this.overpassMapDataDao = overpassMapDataDao;
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{


		List<DirectionOfFlow> directionOfFlows;



		Map<Long, List<DirectionOfFlow>> directionOfFlowMap = new HashMap<>();
		for (DirectionOfFlow flow : directionOfFlows)
		{
			long wayId = flow.wayId;
			if(!directionOfFlowMap.containsKey(wayId))
			{
				directionOfFlowMap.put(wayId, new ArrayList<>());
			}
			directionOfFlowMap.get(wayId).add(flow);
		}

		// TODO merge directionOfFlows

		String overpassQuery = "way(id:" + TextUtils.join(",",directionOfFlowMap.keySet()) + ")";
		overpassMapDataDao.getAndHandleQuota(overpassQuery, (element, geometry) ->
		{
			Way way = (Way) element;
			DirectionOfFlow flow = directionOfFlowMap.get(way.getId());

			List<Long> nodes = way.getNodeIds();

			boolean isForward;
			if(flow.fromNodeId == nodes.get(0) && flow.toNodeId == nodes.get(nodes.size()-1))
			{
				isForward = true;
			}
			else if(flow.toNodeId == nodes.get(0) && flow.fromNodeId == nodes.get(nodes.size()-1))
			{
				isForward = false;
			}
			else
			{
				return;
			}


		});


		return true;
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
	}

	@Nullable @Override public Boolean isApplicableTo(Element element)
	{
		// TODO...
		return null;
	}

	@Override public String getCommitMessage() { return "Add whether this road is a one-way road"; }
	@Override public int getIcon() { return R.drawable.ic_quest_oneway; }
	@Override public int getTitle() { return R.string.quest_oneway_title; }
	@Override public int getDefaultDisabledMessage() { return 0; }
	@Override public int getTitle(@NonNull Map<String, String> tags) { return getTitle(); }
	@NonNull @Override public Countries getEnabledForCountries() { return Countries.ALL; }
}
