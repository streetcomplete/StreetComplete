package de.westnordost.streetcomplete.quests.housenumber;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.AOsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.util.JTSConst;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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
			"out meta geom;";
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
			"out meta geom;";
	}

	private final OverpassMapDataDao overpassServer;

	@Inject public AddHousenumber(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Override public boolean download(BoundingBox bbox, final MapDataWithGeometryHandler handler)
	{
		List<ElementWithGeometry> items = downloadBuildingsWithoutAddresses(bbox);
		if(items == null) return false;
		// empty result: We are done
		if(items.isEmpty()) return true;

		List<Geometry> addrAreas = downloadAreasWithAddresses(bbox);
		if(addrAreas == null) return false;

		Envelope bounds = null;
		for (ElementWithGeometry item : items)
		{
			Envelope addBounds = item.geometry.getEnvelopeInternal();
			if(bounds == null) bounds = addBounds;
			else               bounds.expandToInclude(addBounds);
		}
		// see #885: The area in which the app should search for address nodes (and areas) must be
		// adjusted to the bounding box of all the buildings found. The found buildings may in parts
		// not be within the specified bounding box. But in exactly that part, there may be an
		// address
		BoundingBox adjustedBbox = JTSConst.toBoundingBox(bounds);

		ArrayList<Point> addrPositions = downloadFreeFloatingPositionsWithAddresses(adjustedBbox);
		if(addrPositions == null) return false;

		for (ElementWithGeometry item : items)
		{
			// exclude buildings with housenumber-nodes inside them
			int index = indexOfPointIn(item.geometry, addrPositions);
			if(index != -1)
			{
				// performance improvement: one housenumber-node cannot be covered by multiple
				// buildings. So, it can be removed to reduce the amount of remaining
				// point-in-polygon checks
				addrPositions.remove(index);
				continue;
			}
			// further exclude buildings that are contained in an area with a housenumber
			if(coveredByAnyArea(item.geometry, addrAreas)) continue;

			handler.handle(item.element, item.elementGeometry);
		}

		return true;
    }

    private List<ElementWithGeometry> downloadBuildingsWithoutAddresses(BoundingBox bbox)
	{
		final List<ElementWithGeometry> list = new ArrayList<>();
		String buildingsWithoutAddressesQuery = getBuildingsWithoutAddressesOverpassQuery(bbox);
		boolean success = overpassServer.getAndHandleQuota(buildingsWithoutAddressesQuery, (element, geometry) ->
		{
			if (geometry != null)
			{
				Geometry g = JTSConst.toGeometry(geometry);
				// invalid geometry out of other reasons? (Not sure when this can happen...)
				if (g.isValid())
				{
					ElementWithGeometry item = new ElementWithGeometry();
					item.element = element;
					item.geometry = g;
					item.elementGeometry = geometry;
					list.add(item);
				}
			}
		});
		return success ? list : null;
	}

	private ArrayList<Point> downloadFreeFloatingPositionsWithAddresses(BoundingBox bbox)
	{
		final ArrayList<Point> coords = new ArrayList<>();
		String query = getFreeFloatingAddressesOverpassQuery(bbox);
		boolean success = overpassServer.getAndHandleQuota(query, (element, geometry) ->
		{
			if(geometry != null)
			{
				coords.add(JTSConst.toPoint(geometry.center));
			}
		});
		return success ? coords : null;
	}

	private ArrayList<Geometry> downloadAreasWithAddresses(BoundingBox bbox)
	{
		final ArrayList<Geometry> areas = new ArrayList<>();
		String query = getNonBuildingAreasWithAddresses(bbox);
		boolean success = overpassServer.getAndHandleQuota(query, (element, geometry) ->
		{
			if(geometry != null)
			{
				areas.add(JTSConst.toGeometry(geometry));
			}
		});
		return success ? areas : null;
	}

	private static int indexOfPointIn(Geometry building, ArrayList<Point> points)
	{
		for (int i = 0; i < points.size(); i++)
		{
			Point pos = points.get(i);
			if(building.covers(pos)) return i;
		}
		return -1;
	}

	private static boolean coveredByAnyArea(Geometry building, List<Geometry> areas)
	{
		for (Geometry area : areas)
		{
			if(building.coveredBy(area)) return true;
		}
		return false;
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
		ElementGeometry elementGeometry;
		Geometry geometry;
	}
}
