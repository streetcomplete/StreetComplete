package de.westnordost.streetcomplete.quests.religion;

import android.os.Bundle;

import java.util.ArrayList;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public abstract class AbstractAddReligionToQuestType extends SimpleOverpassQuestType
{
	public AbstractAddReligionToQuestType(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddReligionToPlaceOfWorshipForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddReligionToPlaceOfWorshipForm.OSM_VALUES);
		if(values != null && !values.isEmpty())
		{
			String religionValueStr = values.get(0);
			changes.add("religion", religionValueStr);
		}
	}

	@Override public int getIcon() { return R.drawable.ic_quest_religion; }
}
