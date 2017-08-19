package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddVegan extends SimpleOverpassQuestType
{
	@Inject public AddVegan(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways with ( amenity ~ restaurant|cafe|pub|fast_food|bar ) and diet:vegetarian and !diet:vegan";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddVeganForm();
	}

	public void applyAnswerTo(Bundle bundle, StringMapChangesBuilder changes)
	{
		String other = bundle.getString(AddVeganForm.OTHER_ANSWER);
		if (other != null)
		{
			changes.add("diet:vegan", other);
		}
		else
		{
			changes.add("diet:vegan", bundle.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no");
		}
	}

	@Override public String getCommitMessage() { return "Add vegan diet type"; }
	@Override public int getIcon() { return R.drawable.ic_quest_apple; }
}
