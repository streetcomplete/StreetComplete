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
		String name = element != null && element.getTags() != null ? element.getTags().get("name") : null;
		String type = element.getTags().get("amenity");
		if (type == null) {type = element.getTags().get("railway");}
		String typeString;

		switch (type)
		{
			case "bus_station":
				typeString = getString(R.string.element_bus_station);
			case "station":
				typeString = getString(R.string.element_railway_station);
				break;
			case "subway_entrance":
				typeString = getString(R.string.element_subway_entrance);
				break;
			default:
				typeString = getString(R.string.element_location);
		}

		if(name != null && !name.trim().isEmpty())
		{
			setTitle(R.string.quest_wheelchairAccess_name_type_title, typeString, name);
		} else {
			setTitle(R.string.quest_wheelchairAccess_type_title, typeString);
		}
	}
}
