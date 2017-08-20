package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddVegetarian extends SimpleOverpassQuestType
{
	@Inject public AddVegetarian(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways with amenity ~ restaurant|cafe|fast_food and name and !diet:vegetarian";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddVegetarianForm();
	}

	public void applyAnswerTo(Bundle bundle, StringMapChangesBuilder changes)
	{
		String answer = bundle.getString(AddVegetarianForm.ANSWER);
		if (answer != null) {
			switch (answer)
			{
				case AddVeganForm.ONLY:
					changes.add("diet:vegetarian", "only");
					break;
				case AddVeganForm.YES:
					changes.add("diet:vegetarian", "yes");
					break;
				case AddVeganForm.NO:
					changes.add("diet:vegetarian", "no");
					break;
			}
		}
	}

	@Override public String getCommitMessage() { return "Add vegetarian diet type"; }
	@Override public int getIcon() { return R.drawable.ic_quest_apple; }
}
