package de.westnordost.streetcomplete.quests.car_wash_type;

import android.os.Bundle;

import java.util.List;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddCarWashType extends SimpleOverpassQuestType
{
	@Inject public AddCarWashType(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways with amenity=car_wash and !automated and !self_service";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddCarWashTypeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		List<String> values = answer.getStringArrayList(AddCarWashTypeForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			switch (values.get(0))
			{
				case AddCarWashTypeForm.AUTOMATED:
					changes.add("automated", "yes");
					changes.add("self_service", "no");
					break;
				case AddCarWashTypeForm.SELF_SERVICE:
					changes.add("automated", "no");
					changes.add("self_service", "yes");
					break;
			}
		} else if (values != null && values.size() == 2)
		{
			changes.add("automated", "yes");
			changes.add("self_service", "yes");
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add car wash type";
	}

	@Override public int getIcon() { return R.drawable.ic_quest_parking; }
}
