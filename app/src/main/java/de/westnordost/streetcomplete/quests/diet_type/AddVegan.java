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
		return "nodes, ways with amenity ~ restaurant|cafe|fast_food and name and diet:vegetarian ~ yes|only and !diet:vegan";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		AbstractQuestAnswerFragment form =  new AddDietTypeForm();
		String name = form.getElementName();
		if(name != null)
		{
			form.setTitle(R.string.quest_dietType_vegan_name_title, name);
		}
		return form;
	}

	public void applyAnswerTo(Bundle bundle, StringMapChangesBuilder changes)
	{
		String answer = bundle.getString(AddDietTypeForm.ANSWER);
		if (answer != null) {
			switch (answer)
			{
				case AddDietTypeForm.ONLY:
					changes.add("diet:vegan", "only");
					break;
				case AddDietTypeForm.YES:
					changes.add("diet:vegan", "yes");
					break;
				case AddDietTypeForm.NO:
					changes.add("diet:vegan", "no");
					break;
			}
		}
	}

	@Override public String getCommitMessage() { return "Add vegan diet type"; }
	@Override public int getIcon() { return R.drawable.ic_quest_apple; }
}
