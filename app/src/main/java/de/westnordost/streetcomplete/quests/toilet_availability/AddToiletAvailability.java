package de.westnordost.streetcomplete.quests.toilet_availability;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.util.JTSConst;

public class AddToiletAvailability implements OsmElementQuestType
{

	private final OverpassMapDataDao overpassServer;

	@Inject public AddToiletAvailability(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	/** Query that returns all elements that don't have a toilet tag set */
	private static String getElementsWithoutToiletsOverpassQuery(BoundingBox bbox)
	{
		// only for malls and big stores because users should not need to go inside a non-public
		// place to solve the quest. (Considering malls and department stores public enough)
		return new FiltersParser().parse("nodes, ways with (shop ~ mall|department_store and name and !toilets) or (highway=rest_area and !toilets)").toOverpassQLString(bbox);
	}

	/** Query that returns all toilets */
	private static String getFreeFloatingToiletsOverpassQuery(BoundingBox bbox)
	{
		return new FiltersParser().parse("nodes, ways with amenity=toilets").toOverpassQLString(bbox);
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("toilets", yesno);
	}

	@Nullable @Override public Boolean isApplicableTo(Element element) { return null; }

	@Override public boolean download(BoundingBox bbox, final MapDataWithGeometryHandler handler)
	{
		List<AddHousenumber.ElementWithGeometry> items = downloadElementsWithoutToilets(bbox);
		if(items == null) return false;
		// empty result: We are done
		if(items.isEmpty()) return true;

		Envelope bounds = null;
		for (AddHousenumber.ElementWithGeometry item : items)
		{
			Envelope addBounds = item.geometry.getEnvelopeInternal();
			if(bounds == null) bounds = addBounds;
			else               bounds.expandToInclude(addBounds);
		}
		BoundingBox adjustedBbox = JTSConst.toBoundingBox(bounds);

		ArrayList<Point> toiletsPositions = downloadFreeFloatingPositionsWithToilets(adjustedBbox);
		if(toiletsPositions == null) return false;

		for (AddHousenumber.ElementWithGeometry item : items)
		{
			// exclude elements with toilets inside them
			int index = AddHousenumber.indexOfPointIn(item.geometry, toiletsPositions);
			if(index != -1)
			{
				toiletsPositions.remove(index);
				continue;
			}

			handler.handle(item.element, item.elementGeometry);
		}

		return true;
	}

	private List<AddHousenumber.ElementWithGeometry> downloadElementsWithoutToilets(BoundingBox bbox)
	{
		final List<AddHousenumber.ElementWithGeometry> list = new ArrayList<>();
		String elementsWithoutToiletsOverpassQuery = getElementsWithoutToiletsOverpassQuery(bbox);
		boolean success = overpassServer.getAndHandleQuota(elementsWithoutToiletsOverpassQuery, (element, geometry) ->
		{
			if (geometry != null)
			{
				Geometry g = JTSConst.toGeometry(geometry);
				if (g.isValid())
				{
					AddHousenumber.ElementWithGeometry item = new AddHousenumber.ElementWithGeometry();
					item.element = element;
					item.geometry = g;
					item.elementGeometry = geometry;
					list.add(item);
				}
			}
		});
		return success ? list : null;
	}

	private ArrayList<Point> downloadFreeFloatingPositionsWithToilets(BoundingBox bbox)
	{
		final ArrayList<Point> coords = new ArrayList<>();
		String query = getFreeFloatingToiletsOverpassQuery(bbox);
		boolean success = overpassServer.getAndHandleQuota(query, (element, geometry) ->
		{
			if(geometry != null)
			{
				coords.add(JTSConst.toPoint(geometry.center));
			}
		});
		return success ? coords : null;
	}

	@Override public String getCommitMessage() { return "Add toilet availability"; }
	@Override public int getIcon() { return R.drawable.ic_quest_toilets; }
	@Override public int getTitle() { return R.string.quest_toiletAvailability_name_title; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean isRestArea = "rest_area".equals(tags.get("highway"));
		if (isRestArea) return R.string.quest_toiletAvailability_rest_area_title;
		else return R.string.quest_toiletAvailability_name_title;
	}

	@Override public int getDefaultDisabledMessage() { return 0; }
	@NonNull @Override public Countries getEnabledForCountries() { return Countries.ALL; }
}
