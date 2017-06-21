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
		String highway = null;
		OsmElement element = getOsmElement();
		if (highway == null) {highway = element != null && element.getTags() != null ? element.getTags().get("highway") : null;}
		if (highway == null) {highway = element != null && element.getTags() != null ? element.getTags().get("railway") : null;}
		if (highway == null) {highway = element != null && element.getTags() != null ? element.getTags().get("public_transport") : null;}

		switch (highway) {
			case "bus_stop":
				setTitle(R.string.quest_wheelchairAccess_public_transport_bus_stop_title);
				break;
			case "platform":
				setTitle(R.string.quest_wheelchairAccess_public_transport_platform_title);
				break;
			case "station":
				setTitle(R.string.quest_wheelchairAccess_public_transport_station_title);
				break;
			case "subway_entrance":
				setTitle(R.string.quest_wheelchairAccess_public_transport_entrance_title);
				break;
			default:
				setTitle(R.string.quest_wheelchairAccess_public_transport_default_title);
		}
	}
}
