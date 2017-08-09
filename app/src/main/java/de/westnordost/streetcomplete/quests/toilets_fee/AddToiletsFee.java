package de.westnordost.streetcomplete.quests.toilets_fee;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddToiletsFee extends SimpleOverpassQuestType
{
	@Inject public AddToiletsFee(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways with amenity = toilets and access !~ private|customers and !fee";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		AbstractQuestAnswerFragment form =  new YesNoQuestAnswerFragment();
		form.setTitle(R.string.quest_toiletsFee_title);
		return form;
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("fee", yesno);
	}

	@Override public String getCommitMessage() { return "Add toilets fee"; }
	@Override public int getIcon() { return R.drawable.ic_quest_toilets; }
}
