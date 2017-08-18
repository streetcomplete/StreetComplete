package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddDietType extends SimpleOverpassQuestType
{
	@Inject public AddDietType(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways with ( amenity ~ restaurant|cafe|pub|fast_food|bar ) and !diet:vegetarian and !diet:vegetarian";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddDietTypeForm();
	}

	public void applyAnswerTo(Bundle bundle, StringMapChangesBuilder changes)
	{
		String other = bundle.getString(AddDietTypeForm.OTHER_ANSWER);
		String answer = bundle.getString(AddDietTypeForm.ANSWER);
		if (other != null)
		{
			switch (other)
			{
				case AddDietTypeForm.ONLY_VEGAN:
					changes.add("diet:vegan", "only");
					break;
				case AddDietTypeForm.ONLY_VEGETARIAN:
					changes.add("diet:vegetarian", "only");
					break;
			}
		}
		else
		{
			switch (answer)
			{
				case AddDietTypeForm.VEGETARIAN:
					changes.add("diet:vegetarian", "yes");
					break;
				case AddDietTypeForm.VEGAN:
					changes.add("diet:vegan", "yes");
					break;
				case AddDietTypeForm.NO:
					changes.add("diet:vegetarian", "no");
					changes.add("diet:vegan", "no");
					break;
			}
		}
	}

	@Override public String getCommitMessage() { return "Add diet type"; }
	@Override public int getIcon() { return R.drawable.ic_quest_apple; }
}
