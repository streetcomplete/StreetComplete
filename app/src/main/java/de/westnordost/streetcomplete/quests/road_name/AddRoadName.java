package de.westnordost.streetcomplete.quests.road_name;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.road_name.data.PutRoadNameSuggestionsHandler;
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao;

public class AddRoadName implements OsmElementQuestType
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
		return getOverpassBBox(bbox) + ROADS_WITHOUT_NAMES + "; out meta geom;";
	}

	/** @return overpass query string to get roads with names near roads that don't have names */
	private static String getStreetNameSuggestionsOverpassQuery(BoundingBox bbox)
	{
		return getOverpassBBox(bbox) +
				ROADS_WITHOUT_NAMES + " -> .without_names;" +
				ROADS_WITH_NAMES + " -> .with_names;" +
				"way.with_names(around.without_names:" +
				MAX_DIST_FOR_ROAD_NAME_SUGGESTION + ");" +
				"out meta geom;";
	}

	private static String getOverpassBBox(BoundingBox bbox)
	{
		return "[bbox:" +
				bbox.getMinLatitude() + "," + bbox.getMinLongitude() + "," +
				bbox.getMaxLatitude() + "," + bbox.getMaxLongitude() +
				"];";
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
		if(answer.getBoolean(AddRoadNameForm.NO_NAME))
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

		String[] names = answer.getStringArray(AddRoadNameForm.NAMES);
		String[] languages = answer.getStringArray(AddRoadNameForm.LANGUAGE_CODES);

		HashMap<String,String> roadNameByLanguage = toRoadNameByLanguage(names, languages);
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

	private static HashMap<String,String> toRoadNameByLanguage(String[] names, String[] languages)
	{
		HashMap<String,String> result = new HashMap<>();
		result.put("", names[0]);
		// add languages only if there is more than one name specified. If there is more than one
		// name, the "main" name (name specified in top row) is also added with the language.
		if(names.length > 1)
		{
			for (int i = 0; i < names.length; i++)
			{
				// (the first) element may have no specific language
				if(!languages[i].isEmpty())
				{
					result.put(languages[i], names[i]);
				}
			}
		}
		return result;
	}

	@Override public boolean appliesTo(Element element)
	{
		return ROADS_WITHOUT_NAMES_TFE.matches(element);
	}

	@Override public String getCommitMessage() { return "Determine road names"; }
	@Override public int getIcon() { return R.drawable.ic_quest_street_name; }
	@Override public int getTitle() { return R.string.quest_streetName_title; }
	@Override public int getTitle(Map<String,String> tags) { return getTitle(); }
}
