package de.westnordost.streetcomplete.quests.housenumber;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
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
			" and !addr:housenumber and !addr:housename");

	private static final TagFilterExpression NODES_WITH_HOUSENUMBERS = new FiltersParser().parse(
			" nodes with addr:housenumber or addr:housename");

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
		success = overpassServer.getAndHandleQuota(nodesWithHousenumbersQuery, new MapDataWithGeometryHandler()
		{
			@Override public void handle(@NonNull Element element, @Nullable ElementGeometry geometry)
			{
				if(geometry != null)
				{
					housenumberCoords.add(JTSConst.toPoint(geometry.center));
				}
			}
		});
		if(!success) return false;

		String buildingsWithoutHousenumbersQuery = HOUSES_WITHOUT_HOUSENUMBERS.toOverpassQLString(bbox);
		success = overpassServer.getAndHandleQuota(buildingsWithoutHousenumbersQuery, new MapDataWithGeometryHandler()
		{
			@Override public void handle(@NonNull Element element, @Nullable ElementGeometry geometry)
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
					if(g.covers(p))
					{
						// one housenumber-node cannot be covered by multiple buildings. So, it can
						// be removed to reduce the amount of remaining point-in-polygon checks
						housenumberCoords.remove(i);
						return;
					}
				}
				handler.handle(element, geometry);
			}
		});

		return success;
    }

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String housenumber = answer.getString(AddHousenumberForm.HOUSENUMBER);
		changes.add("addr:housenumber", housenumber);
	}

	@Override public AbstractQuestAnswerFragment createForm() { return new AddHousenumberForm(); }
	@Override public String getCommitMessage() { return "Add housenumbers"; }
	@Override public String getIconName() { return "housenumber"; }
}
