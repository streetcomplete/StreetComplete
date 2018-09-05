package de.westnordost.streetcomplete.quests.housenumber;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.AOsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.util.FlattenIterable;
import de.westnordost.streetcomplete.util.LatLonRaster;
import de.westnordost.streetcomplete.util.SphericalEarthMath;

public class AddHousenumber extends AOsmElementQuestType
{
	private static final String ANY_ADDRESS_FILTER =
		"[~'^addr:(housenumber|housename|conscriptionnumber|streetnumber)$'~'.']";

	private static final String NO_ADDRESS_FILTER =
		"[!'addr:housenumber'][!'addr:housename'][!'addr:conscriptionnumber'][!'addr:streetnumber'][!noaddress]";

	private static final String BUILDINGS_WITHOUT_ADDRESS_FILTER =
		"['building'~'^(house|residential|apartments|detached|terrace|dormitory|semi|semidetached_house|farm|" +
			"school|civic|college|university|public|hospital|kindergarten|train_station|hotel|" +
			"retail|commercial)$'][location!=underground]" + NO_ADDRESS_FILTER;

	/** Query that returns all areas that are not buildings but have addresses */
	private static String getNonBuildingAreasWithAddresses(BoundingBox bbox)
	{
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			"(way[!building]"+ANY_ADDRESS_FILTER+";rel[!building]"+ANY_ADDRESS_FILTER+";);" +
			"out geom;";
	}

	/** Query that returns all buildings that neither have an address node on their outline, nor
	 *  on itself */
	private static String getBuildingsWithoutAddressesOverpassQuery(BoundingBox bbox)
	{
		String bboxFilter = OverpassQLUtil.getOverpassBboxFilter(bbox);
		return
			"(" +
			"  way" + BUILDINGS_WITHOUT_ADDRESS_FILTER + bboxFilter + ";" +
			"  rel" + BUILDINGS_WITHOUT_ADDRESS_FILTER + bboxFilter + ";" +
			") -> .buildings;" +
			".buildings > -> .building_nodes;" +
			"node.building_nodes"+ANY_ADDRESS_FILTER+";< -> .buildings_with_addr_nodes;" +
			// all buildings without housenumber minus ways that contain building nodes with addresses
			"(.buildings; - .buildings_with_addr_nodes;);" +
			// not using OverpassQLUtil.getQuestPrintStatement here because buildings will get filtered out further here
			"out meta geom;";
	}

	/** Query that returns all address nodes that are not part of any building outline */
	private static String getFreeFloatingAddressesOverpassQuery(BoundingBox bbox)
	{
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			"(" +
			"  node"+ANY_ADDRESS_FILTER+";" +
			"  - ((way[building];relation[building];);>;);" +
			");"+
			"out skel;";
	}

	private final OverpassMapDataDao overpassServer;

	@Inject public AddHousenumber(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Override public boolean download(BoundingBox bbox, final MapDataWithGeometryHandler handler)
	{
		long ms = System.currentTimeMillis();
		Map<LatLon, ElementWithGeometry> buildings = downloadBuildingsWithoutAddresses(bbox);
		if(buildings == null) return false;
		// empty result: We are done
		if(buildings.isEmpty()) return true;

		List<ElementGeometry> addrAreas = downloadAreasWithAddresses(bbox);
		if(addrAreas == null) return false;

		bbox = getBoundingBoxThatIncludes(buildings.values());

		LatLonRaster addrPositions = downloadFreeFloatingPositionsWithAddresses(bbox);
		if(addrPositions == null) return false;

		Log.d("AddHousenumber", "Downloaded "+buildings.size()+" buildings with no address, " +
			addrAreas.size() + " areas with address and " +
			addrPositions.size() + " address nodes" +
			" in " + (System.currentTimeMillis()-ms) + "ms");
		ms = System.currentTimeMillis();

		LatLonRaster buildingPositions = new LatLonRaster(bbox, 0.0005);
		for (LatLon buildingCenter : buildings.keySet())
		{
			buildingPositions.insert(buildingCenter);
		}

		// exclude buildings that are contained in an area with a housenumber
		for (ElementGeometry addrArea : addrAreas)
		{
			for (LatLon buildingPos : buildingPositions.getAll(addrArea.getBounds()))
			{
				if(SphericalEarthMath.isInMultipolygon(buildingPos, addrArea.polygons))
				{
					buildings.remove(buildingPos);
				}
			}
		}

		int createdQuests = 0;
		// only buildings with no housenumber-nodes inside them
		for (ElementWithGeometry building : buildings.values())
		{
			// even though we could continue here, limit the max amount of quests created to the
			// default maximum to avoid performance problems
			if(createdQuests++ >= OverpassQLUtil.DEFAULT_MAX_QUESTS) break;

			LatLon addrContainedInBuilding = getPositionContainedInBuilding(building.geometry, addrPositions);
			if(addrContainedInBuilding != null)
			{
				addrPositions.remove(addrContainedInBuilding);
				continue;
			}

			handler.handle(building.element, building.geometry);
		}

		Log.d("AddHousenumber", "Processing data took " + (System.currentTimeMillis()-ms) + "ms");

		return true;
    }

    private Map<LatLon, ElementWithGeometry> downloadBuildingsWithoutAddresses(BoundingBox bbox)
	{
		final Map<LatLon, ElementWithGeometry> buildingsByCenterPoint = new HashMap<>();
		String buildingsWithoutAddressesQuery = getBuildingsWithoutAddressesOverpassQuery(bbox);
		boolean success = overpassServer.getAndHandleQuota(buildingsWithoutAddressesQuery, (element, geometry) ->
		{
			if (geometry != null && geometry.polygons != null)
			{
				ElementWithGeometry item = new ElementWithGeometry();
				item.element = element;
				item.geometry = geometry;
				buildingsByCenterPoint.put(geometry.center, item);
			}
		});
		return success ? buildingsByCenterPoint : null;
	}

	private LatLonRaster downloadFreeFloatingPositionsWithAddresses(BoundingBox bbox)
	{
		final LatLonRaster grid = new LatLonRaster(bbox, 0.0005);
		String query = getFreeFloatingAddressesOverpassQuery(bbox);
		boolean success = overpassServer.getAndHandleQuota(query, (element, geometry) ->
		{
			if(geometry != null) grid.insert(geometry.center);
		});
		return success ? grid : null;
	}

	private ArrayList<ElementGeometry> downloadAreasWithAddresses(BoundingBox bbox)
	{
		final ArrayList<ElementGeometry> areas = new ArrayList<>();
		String query = getNonBuildingAreasWithAddresses(bbox);
		boolean success = overpassServer.getAndHandleQuota(query, (element, geometry) ->
		{
			if(geometry != null && geometry.polygons != null) areas.add(geometry);
		});
		return success ? areas : null;
	}

	private BoundingBox getBoundingBoxThatIncludes(Iterable<ElementWithGeometry> buildings)
	{
		// see #885: The area in which the app should search for address nodes (and areas) must be
		// adjusted to the bounding box of all the buildings found. The found buildings may in parts
		// not be within the specified bounding box. But in exactly that part, there may be an
		// address
		FlattenIterable<LatLon> allThePoints = new FlattenIterable<>(LatLon.class);
		for (ElementWithGeometry building : buildings)
		{
			allThePoints.add(building.geometry.polygons);
		}
		return SphericalEarthMath.enclosingBoundingBox(allThePoints);
	}

	private static LatLon getPositionContainedInBuilding(ElementGeometry building, LatLonRaster positions)
	{
		List<List<LatLon>> buildingPolygons = building.polygons;
		if(buildingPolygons == null) return null;

		for (LatLon pos : positions.getAll(building.getBounds()))
		{
			if(SphericalEarthMath.isInMultipolygon(pos, buildingPolygons)) return pos;
		}
		return null;
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		boolean noAddress = answer.getBoolean(AddHousenumberForm.NO_ADDRESS);
		String housenumber = answer.getString(AddHousenumberForm.HOUSENUMBER);
		String housename = answer.getString(AddHousenumberForm.HOUSENAME);
		String conscriptionnumber = answer.getString(AddHousenumberForm.CONSCRIPTIONNUMBER);
		String streetnumber = answer.getString(AddHousenumberForm.STREETNUMBER);

		if(noAddress) {
			changes.add("noaddress", "yes");
		}
		else if(conscriptionnumber != null)
		{
			changes.add("addr:conscriptionnumber", conscriptionnumber);
			if(!TextUtils.isEmpty(streetnumber)) changes.add("addr:streetnumber", streetnumber);

			housenumber = streetnumber;
			if(TextUtils.isEmpty(housenumber)) housenumber = conscriptionnumber;
			changes.add("addr:housenumber", housenumber);
		}
		else if(housenumber != null)
		{
			changes.add("addr:housenumber", housenumber);
		}
		else if(housename != null)
		{
			changes.add("addr:housename", housename);
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

	@Override public AbstractQuestAnswerFragment createForm() { return new AddHousenumberForm(); }
	@Override public String getCommitMessage() { return "Add housenumbers"; }
	@Override public int getIcon() { return R.drawable.ic_quest_housenumber; }
	@Override public int getTitle(@NonNull Map<String,String> tags) { return R.string.quest_address_title; }

	@NonNull @Override public Countries getEnabledForCountries()
	{
		// See overview here: https://ent8r.github.io/blacklistr/?java=housenumber/AddHousenumber.java

		return Countries.allExcept(new String[]{
				"NL", // https://forum.openstreetmap.org/viewtopic.php?id=60356
				"DK", // https://lists.openstreetmap.org/pipermail/talk-dk/2017-November/004898.html
				"NO", // https://forum.openstreetmap.org/viewtopic.php?id=60357
				"CZ", // https://lists.openstreetmap.org/pipermail/talk-cz/2017-November/017901.html
				"IT"  // https://lists.openstreetmap.org/pipermail/talk-it/2018-July/063712.html
		});
	}

	private static class ElementWithGeometry
	{
		Element element;
		ElementGeometry geometry;
	}
}
