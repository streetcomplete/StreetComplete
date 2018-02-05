package de.westnordost.streetcomplete.quests.housenumber;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
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
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.util.JTSConst;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class AddHousenumber implements OsmElementQuestType
{
	private static final TagFilterExpression HOUSES_WITHOUT_HOUSENUMBERS = new FiltersParser().parse(
			" ways, relations with " +
			" building ~ house|residential|apartments|detached|terrace|hotel|dormitory|houseboat|" +
			            "school|civic|college|university|public|hospital|kindergarten|train_station|" +
			            "retail|commercial" +
			" and !addr:housenumber and !addr:housename and !addr:conscriptionnumber and !addr:streetnumber");

	private static final TagFilterExpression NODES_WITH_HOUSENUMBERS = new FiltersParser().parse(
			" nodes with addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber");

	private static final TagFilterExpression NON_BUILDING_AREAS_WITH_HOUSENUMBERS = new FiltersParser().parse(
			"ways, relations with !building and (addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber)");

	private final OverpassMapDataDao overpassServer;

	@Inject public AddHousenumber(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Override public boolean download(BoundingBox bbox, final MapDataWithGeometryHandler handler)
	{
		boolean success;

		final ArrayList<Point> housenumberCoords = new ArrayList<>();
		String nodesWithHousenumbersQuery = NODES_WITH_HOUSENUMBERS.toOverpassQLString(bbox);
		success = overpassServer.getAndHandleQuota(nodesWithHousenumbersQuery, (element, geometry) ->
		{
			if(geometry != null)
			{
				housenumberCoords.add(JTSConst.toPoint(geometry.center));
			}
		});
		if(!success) return false;

		final ArrayList<Geometry> areasWithHousenumber = new ArrayList<>();
		String areasWithHousenumbersQuery = NON_BUILDING_AREAS_WITH_HOUSENUMBERS.toOverpassQLString(bbox);
		success = overpassServer.getAndHandleQuota(areasWithHousenumbersQuery, (element, geometry) ->
		{
			if(geometry != null)
			{
				areasWithHousenumber.add(JTSConst.toGeometry(geometry));
			}
		});
		if(!success) return false;

		String buildingsWithoutHousenumbersQuery = HOUSES_WITHOUT_HOUSENUMBERS.toOverpassQLString(bbox);
		success = overpassServer.getAndHandleQuota(buildingsWithoutHousenumbersQuery, (element, geometry) ->
		{
			// invalid geometry
			if(geometry == null) return;

			Geometry g = JTSConst.toGeometry(geometry);
			// invalid geometry out of other reasons? (Not sure when this can happen...)
			if(!g.isValid()) return;

			// exclude buildings with housenumber-nodes inside them
			for(int i = 0; i < housenumberCoords.size(); ++i)
			{
				Point p = housenumberCoords.get(i);
				// this line is a very expensive computation
				if(g.covers(p))
				{
					// one housenumber-node cannot be covered by multiple buildings. So, it can
					// be removed to reduce the amount of remaining point-in-polygon checks
					housenumberCoords.remove(i);
					return;
				}
			}
			// further exclude buildings that are contained in an area with a housenumber
			for (Geometry areaWithHousenumber : areasWithHousenumber)
			{
				if(g.coveredBy(areaWithHousenumber)) return;
			}

			handler.handle(element, geometry);
		});

		return success;
    }

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String housenumber = answer.getString(AddHousenumberForm.HOUSENUMBER);
		String housename = answer.getString(AddHousenumberForm.HOUSENAME);
		String conscriptionnumber = answer.getString(AddHousenumberForm.CONSCRIPTIONNUMBER);
		String streetnumber = answer.getString(AddHousenumberForm.STREETNUMBER);

		if(conscriptionnumber != null)
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

	@Override public Boolean isApplicableTo(Element element)
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
	@Override public int getTitle(@NonNull Map<String,String> tags) { return getTitle(); }
	@Override public int getTitle() { return R.string.quest_address_title; }

	@Override public int getDefaultDisabledMessage() { return 0; }

	@NonNull @Override public Countries getEnabledForCountries()
	{
		return Countries.allExcept(new String[]{
				"NL", // https://forum.openstreetmap.org/viewtopic.php?id=60356
				"DK", // https://lists.openstreetmap.org/pipermail/talk-dk/2017-November/004898.html
				"NO", // https://forum.openstreetmap.org/viewtopic.php?id=60357
				"CZ"  // https://lists.openstreetmap.org/pipermail/talk-cz/2017-November/017901.html
		});
	}
}
