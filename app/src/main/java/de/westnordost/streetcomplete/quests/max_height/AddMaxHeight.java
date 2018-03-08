package de.westnordost.streetcomplete.quests.max_height;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddMaxHeight implements OsmElementQuestType
{

	private final OverpassMapDataDao overpassServer;

	@Inject public AddMaxHeight(OverpassMapDataDao overpassServer)
	{
		this.overpassServer = overpassServer;
	}

	/*the query does not exclude elements which are not accessible by the public,
	because a maxheight sign will very probably be visible at the entrance or beginning*/
	private static final String QUERY_RESTRICTIONS = "['maxheight'!~'.']['maxheight:physical'!~'.']";

	private static final String ROADS = "(primary|secondary|tertiary|trunk)(_link)?|motorway|service|residential|unclassified|living_street";

	@Override public boolean download(BoundingBox bbox, MapDataWithGeometryHandler handler)
	{
		return overpassServer.getAndHandleQuota(getOverpassQuery(bbox), handler);
	}

	private static String getOverpassQuery(BoundingBox bbox)
	{
		return OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			"(" +
			" node['barrier'='height_restrictor']" +  QUERY_RESTRICTIONS + ";" +
			" node['amenity'='parking_entrance']['parking'~'^(underground|multi-storey)$']" +  QUERY_RESTRICTIONS + ";" +
			" way['highway'~'^(" + ROADS + ")$']['tunnel'~'^(yes|building_passage)$']" +  QUERY_RESTRICTIONS + ";" +
			" way['highway'~'^(" + ROADS + ")$']['covered'='yes']" +  QUERY_RESTRICTIONS + ";" +
			");" +
			"out meta geom;";
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddMaxHeightForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		boolean hasNoSign = answer.getBoolean(AddMaxHeightForm.NO_SIGN);
		String maxheight = answer.getString(AddMaxHeightForm.MAX_HEIGHT);
		if(hasNoSign)
		{
			/*TODO: maxheight=default or maxheight=no_sign?
			according to https://taginfo.openstreetmap.org/keys/maxheight#values maxheight=no_sign is only used 14 times,
			while maxheight=default is used about 10K times*/
			changes.add("maxheight", "default");
		}
		else
		{
			if (maxheight != null)
			{
				changes.add("maxheight", maxheight);
			}
		}
	}

	@Override public String getCommitMessage() { return "Add maximum height"; }
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

	@Override public int getTitle() { return R.string.quest_maxheight_title; }
	@Override public int getDefaultDisabledMessage() { return 0; }
	@Nullable @Override public Boolean isApplicableTo(Element element) { return null; }
	@NonNull @Override public Countries getEnabledForCountries() { return Countries.ALL; }
}
