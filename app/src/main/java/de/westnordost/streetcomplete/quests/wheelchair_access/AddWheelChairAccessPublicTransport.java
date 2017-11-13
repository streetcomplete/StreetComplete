package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;

public class AddWheelChairAccessPublicTransport extends SimpleOverpassQuestType
{
	@Inject public AddWheelChairAccessPublicTransport(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return " nodes, ways, relations with " +
				" (amenity = bus_station or " +
				" railway ~ station|subway_entrance)" +
				" and !wheelchair";
	}

	@Override public WheelchairAccessAnswerFragment createForm()
	{
		return new AddWheelchairAccessPublicTransportForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String wheelchair = answer.getString(AddWheelchairAccessPublicTransportForm.ANSWER);
		if(wheelchair != null)
		{
			changes.add("wheelchair", wheelchair);
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add wheelchair access to public transport platforms";
	}
	@Override public int getIcon() { return R.drawable.ic_quest_wheelchair; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean hasName = tags.containsKey("name");
		String type = tags.get("amenity");
		if (type == null) type = tags.get("railway");
		if (type == null) type = "";

		if(hasName)
		{
			switch (type)
			{
				case "bus_station":     return R.string.quest_wheelchairAccess_bus_station_name_title;
				case "station":         return R.string.quest_wheelchairAccess_railway_station_name_title;
				case "subway_entrance": return R.string.quest_wheelchairAccess_subway_entrance_name_title;
				default:                return R.string.quest_wheelchairAccess_location_name_title;
			}
		} else {
			switch (type)
			{
				case "bus_station":     return R.string.quest_wheelchairAccess_bus_station_title;
				case "station":         return R.string.quest_wheelchairAccess_railway_station_title;
				case "subway_entrance": return R.string.quest_wheelchairAccess_subway_entrance_title;
				default:                return R.string.quest_wheelchairAccess_location_title;
			}
		}
	}
}
