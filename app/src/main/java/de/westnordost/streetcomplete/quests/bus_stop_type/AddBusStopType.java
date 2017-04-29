package de.westnordost.streetcomplete.quests.bus_stop_type;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBusStopType extends OverpassQuestType
{
	@Override
	protected String getTagFilters()
	{
		return "nodes with public_transport=platform and !shelter and !informal";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddBusStopTypeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		switch(answer.getString(AddBusStopTypeForm.BUS_STOP_TYPE)) {
			case "informal":
				changes.add("informal", "yes");
				break;

			case "pole":
				changes.add("shelter", "no");
				break;

			case "shelter":
				changes.add("shelter", "yes");
				break;
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add bus stop type";
	}

	@Override public String getIconName() {	return "bus_stop"; }
}
