package de.westnordost.streetcomplete.quests.localized_name;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.AOsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler;
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao;

public class AddRoadName extends AOsmElementQuestType
{
	public static final double MAX_DIST_FOR_ROAD_NAME_SUGGESTION = 30; //m

	private static final String ROADS =	TextUtils.join("|", new String[]{
			"living_street", "residential", "pedestrian", "primary", "secondary", "tertiary", "unclassified"
	});
	private static final String ROADS_WITH_NAMES = "way[highway~\"^("+ROADS+")$\"][name]";
	private static final String ROADS_WITHOUT_NAMES =
			"way[highway~\"^("+ROADS+")$\"][!name][!ref][noname != yes][!junction][!area]";
	// this must be the same as above but in tag filter expression syntax
	private static final TagFilterExpression ROADS_WITHOUT_NAMES_TFE = new FiltersParser().parse(
			"ways with highway~" + ROADS + " and !name and !ref and noname != yes and !junction and !area");

	/** @return overpass query string for creating the quests */
	private static String getOverpassQuery(BoundingBox bbox)
	{
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			ROADS_WITHOUT_NAMES + "; " +
			OverpassQLUtil.getQuestPrintStatement();
	}

	/** @return overpass query string to get roads with names near roads that don't have names */
	private static String getStreetNameSuggestionsOverpassQuery(BoundingBox bbox)
	{
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
				ROADS_WITHOUT_NAMES + " -> .without_names;" +
				ROADS_WITH_NAMES + " -> .with_names;" +
				"way.with_names(around.without_names:" +
				MAX_DIST_FOR_ROAD_NAME_SUGGESTION + ");" +
				"out body geom;";
	}

	private final RoadNameSuggestionsDao roadNameSuggestionsDao;
	private final OverpassMapDataDao overpassServer;
	private final PutRoadNameSuggestionsHandler putRoadNameSuggestionsHandler;

	@Inject public AddRoadName(OverpassMapDataDao overpassServer,
							   RoadNameSuggestionsDao roadNameSuggestionsDao,
							   PutRoadNameSuggestionsHandler putRoadNameSuggestionsHandler)
	{
		this.overpassServer = overpassServer;
		this.roadNameSuggestionsDao = roadNameSuggestionsDao;
		this.putRoadNameSuggestionsHandler = putRoadNameSuggestionsHandler;
	}

	public boolean download(BoundingBox bbox, final MapDataWithGeometryHandler handler)
	{
		return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler) &&
				overpassServer.getAndHandleQuota(getStreetNameSuggestionsOverpassQuery(bbox), putRoadNameSuggestionsHandler);
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddRoadNameForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(AddLocalizedNameForm.NO_NAME))
		{
			changes.add("noname", "yes");
			return;
		}

		int noProperRoad = answer.getInt(AddRoadNameForm.NO_PROPER_ROAD);
		if(noProperRoad != 0)
		{
			if(noProperRoad == AddRoadNameForm.IS_SERVICE)
				changes.modify("highway", "service");
			else if(noProperRoad == AddRoadNameForm.IS_TRACK)
				changes.modify("highway", "track");
			else if(noProperRoad == AddRoadNameForm.IS_LINK)
			{
				String prevValue = changes.getPreviousValue("highway");
				if(prevValue.matches("primary|secondary|tertiary"))
				{
					changes.modify("highway", prevValue + "_link");
				}
			}
			return;
		}

		HashMap<String,String> roadNameByLanguage = AddLocalizedNameForm.toNameByLanguage(answer);
		for (Map.Entry<String, String> e : roadNameByLanguage.entrySet())
		{
			if(e.getKey().isEmpty())
			{
				changes.add("name", e.getValue());
			}
			else
			{
				changes.add("name:" + e.getKey(), e.getValue());
			}
		}

		// these params are passed from the form only to update the road name suggestions so that
		// newly input street names turn up in the suggestions as well

		long wayId = answer.getLong(AddRoadNameForm.WAY_ID);
		ElementGeometry geometry = (ElementGeometry) answer.getSerializable(AddRoadNameForm.WAY_GEOMETRY);
		if (geometry != null && geometry.polylines != null && !geometry.polylines.isEmpty())
		{
			roadNameSuggestionsDao.putRoad(wayId, roadNameByLanguage,
					new ArrayList<>(geometry.polylines.get(0)));
		}
	}

	@Nullable @Override public Boolean isApplicableTo(Element element)
	{
		return ROADS_WITHOUT_NAMES_TFE.matches(element);
	}

	@Override public String getCommitMessage() { return "Determine road names and types"; }
	@Override public int getIcon() { return R.drawable.ic_quest_street_name; }
	@Override public int getTitle(@NonNull Map<String,String> tags)
	{
		boolean isPedestrian = "pedestrian".equals(tags.get("highway"));
		if (isPedestrian) return R.string.quest_streetName_pedestrian_title;
		else return R.string.quest_streetName_title;
	}
}
