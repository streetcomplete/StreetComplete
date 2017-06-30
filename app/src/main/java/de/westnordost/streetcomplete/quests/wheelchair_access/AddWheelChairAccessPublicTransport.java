package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
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
				" amenity = bus_station or " +
				" railway ~ station|subway_entrance" +
				" and !wheelchair";
	}

	@Override public int importance()
	{
		return QuestImportance.MAJOR;
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

	@Override public String getIconName() {	return "wheelchair"; }
}
