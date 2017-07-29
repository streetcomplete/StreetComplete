package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;

public class AddWheelchairAccessPublicTransportForm extends WheelchairAccessAnswerFragment
{
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setContentView(R.layout.quest_wheelchair_public_transport_explanation);
		setTitle();
		return view;
	}

	private void setTitle()
	{
		OsmElement element = getOsmElement();
		String name = getElementName();
		String type = element.getTags().get("amenity");
		if (type == null) {type = element.getTags().get("railway");}

		if(name != null)
		{
			switch (type)
			{
				case "bus_station":
					setTitle(R.string.quest_wheelchairAccess_bus_station_name_title, name);
					break;
				case "station":
					setTitle(R.string.quest_wheelchairAccess_railway_station_name_title, name);
					break;
				case "subway_entrance":
					setTitle(R.string.quest_wheelchairAccess_subway_entrance_name_title, name);
					break;
				default:
					setTitle(R.string.quest_wheelchairAccess_location_name_title, name);
			}
		} else {
			switch (type)
			{
				case "bus_station":
					setTitle(R.string.quest_wheelchairAccess_bus_station_title);
					break;
				case "station":
					setTitle(R.string.quest_wheelchairAccess_railway_station_title);
					break;
				case "subway_entrance":
					setTitle(R.string.quest_wheelchairAccess_subway_entrance_title);
					break;
				default:
					setTitle(R.string.quest_wheelchairAccess_location_title);
			}
		}
	}
}
