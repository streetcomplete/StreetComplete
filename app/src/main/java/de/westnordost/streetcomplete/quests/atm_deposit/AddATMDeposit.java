package de.westnordost.streetcomplete.quests.atm_deposit;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddATMDeposit extends SimpleOverpassQuestType
{
	@Inject public AddATMDeposit(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes with amenity=atm and !cash_in";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("cash_in", yesno);
	}

	@Override public String getCommitMessage() { return "Specify whether ATM can be used to deposit cash."; }
	@Override public int getIcon() { return R.drawable.ic_quest_atm_deposit; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		boolean hasOperator = tags.containsKey("operator");
		if(hasOperator) return R.string.quest_atm_deposit_title_operator;
		else            return R.string.quest_atm_deposit_title;
	}
}
