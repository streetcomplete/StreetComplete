package de.westnordost.streetcomplete.quests.max_height;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.AOsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.util.Lazy;

public class AddMaxHeight extends AOsmElementQuestType
{
	private final OverpassMapDataDao overpassServer;

	private static final Lazy<TagFilterExpression> NODE_FILTER = new Lazy<>(() -> new FiltersParser().parse(
		"nodes with" +
		" (barrier=height_restrictor or amenity=parking_entrance and parking ~ underground|multi-storey)" +
		" and !maxheight and !maxheight:physical"
	));

	private static final Lazy<TagFilterExpression> WAY_FILTER = new Lazy<>(() -> new FiltersParser().parse(
		"ways with" +
		" (highway ~ motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|pedestrian|track|road" +
		" or (highway=service and access!=private))" +
		" and (covered=yes or tunnel~yes|building_passage|avalanche_protector)" +
		" and !maxheight and !maxheight:physical"
	));

	@Inject public AddMaxHeight(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler);
	}

	private static String getOverpassQuery(BoundingBox bbox)
	{
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			NODE_FILTER.get().toOverpassQLString(null) +
			WAY_FILTER.get().toOverpassQLString(null) +
			OverpassQLUtil.getQuestPrintStatement();
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddMaxHeightForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String maxHeight = answer.getString(AddMaxHeightForm.MAX_HEIGHT);
		String noSign = answer.getString(AddMaxHeightForm.NO_SIGN);

		if (maxHeight != null)
		{
			changes.add("maxheight", maxHeight);
		} else if (noSign != null)
		{
			if(noSign.equals(AddMaxHeightForm.BELOW_DEFAULT))
				changes.add("maxheight", "below_default");
			else if(noSign.equals(AddMaxHeightForm.DEFAULT))
				changes.add("maxheight", "default");
		}
	}

	@Nullable @Override public Boolean isApplicableTo(Element element)
	{
		return NODE_FILTER.get().matches(element) || WAY_FILTER.get().matches(element);
	}

	@Override public String getCommitMessage() { return "Add maximum heights"; }
	@Override public int getIcon() { return R.drawable.ic_quest_max_height; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean isParkingEntrance = "parking_entrance".equals(tags.get("amenity"));
		boolean isHeightRestrictor = "height_restrictor".equals(tags.get("barrier"));
		boolean isTunnel = "yes".equals(tags.get("tunnel"));

		if (isParkingEntrance) return R.string.quest_maxheight_parking_entrance_title;
		if (isHeightRestrictor) return R.string.quest_maxheight_height_restrictor_title;
		if (isTunnel) return R.string.quest_maxheight_tunnel_title;

		return R.string.quest_maxheight_title;
	}
}
