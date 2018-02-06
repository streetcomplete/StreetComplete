package de.westnordost.streetcomplete.quests.car_wash_type;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

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
		if(values != null)
		{
			boolean isAutomated = values.contains(AddCarWashTypeForm.AUTOMATED);
			boolean isSelfService = values.contains(AddCarWashTypeForm.SELF_SERVICE);
			boolean isStaffService = values.contains(AddCarWashTypeForm.SERVICE);

			changes.add("automated", isAutomated ? "yes" : "no");
			changes.add("self_service", isSelfService && !isStaffService ? "yes" : "no");
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add car wash type";
	}

	@Override public int getIcon() { return R.drawable.ic_quest_car_wash; }

	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_carWashType_title;
	}
}
